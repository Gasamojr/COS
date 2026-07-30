package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.DEFAULT_STAGES
import com.example.data.model.ServiceOrder
import com.example.data.model.StageHistory
import com.example.data.model.User
import com.example.data.repository.ServiceOrderRepository
import com.example.data.util.CsvImportResult
import com.example.data.util.CsvImporter
import com.example.data.util.DataExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val totalOrders: Int = 0,
    val inProductionCount: Int = 0,
    val delayedCount: Int = 0,
    val finishedCount: Int = 0,
    val dispatchedCount: Int = 0,
    val deliveredCount: Int = 0
)

class ServiceOrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ServiceOrderRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ServiceOrderRepository(
            db.serviceOrderDao(),
            db.stageHistoryDao(),
            db.notificationDao()
        )
        viewModelScope.launch {
            repository.checkSeedData()
        }
    }

    // Users Management State
    val registeredUsers = MutableStateFlow<List<User>>(User.SAMPLE_USERS)

    // Active User State
    val currentUser = MutableStateFlow(User.SAMPLE_USERS[0]) // João Silva (Operador)

    fun setCurrentUser(user: User) {
        currentUser.value = user
    }

    fun registerUser(
        name: String,
        role: String,
        email: String,
        badgeNumber: String,
        pin: String,
        avatarColorHex: String = "#2196F3",
        customPrivileges: Set<String>? = null
    ): User {
        val newId = (registeredUsers.value.size + 1).toString()
        val newUser = User(
            id = newId,
            name = name,
            role = role,
            avatarColorHex = avatarColorHex,
            email = email,
            badgeNumber = badgeNumber,
            pin = pin.ifBlank { "1234" },
            customPrivileges = customPrivileges
        )
        registeredUsers.value = registeredUsers.value + newUser
        currentUser.value = newUser
        return newUser
    }

    fun updateUserPrivileges(userId: String, privileges: Set<String>) {
        registeredUsers.value = registeredUsers.value.map { user ->
            if (user.id == userId) {
                user.copy(customPrivileges = privileges)
            } else {
                user
            }
        }
        if (currentUser.value.id == userId) {
            currentUser.value = currentUser.value.copy(customPrivileges = privileges)
        }
    }

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedStageFilter = MutableStateFlow<Int?>(null)
    val selectedClientFilter = MutableStateFlow<String?>(null)
    val selectedSellerFilter = MutableStateFlow<String?>(null)

    // CSV Import State
    val csvInputText = MutableStateFlow("")
    val csvImportResult = MutableStateFlow<CsvImportResult?>(null)
    val isImportingCsv = MutableStateFlow(false)

    // Selection & Navigation
    val selectedOsNumber = MutableStateFlow<String?>(null)
    val showAdvanceModal = MutableStateFlow(false)
    val advanceNotes = MutableStateFlow("")
    val advanceProducedQuantity = MutableStateFlow("")
    val targetStageForAdvance = MutableStateFlow<Int?>(null)

    // UI Dialogs
    val showNotificationSheet = MutableStateFlow(false)
    val showUserRoleMenu = MutableStateFlow(false)

    // Flow for raw orders
    private val rawOrders = repository.allOrders

    // Available distinct sellers list
    val availableSellers: StateFlow<List<String>> = rawOrders.map { orders ->
        orders.map { it.sellerName.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered orders list
    val filteredOrders: StateFlow<List<ServiceOrder>> = combine(
        rawOrders,
        searchQuery,
        selectedStatusFilter,
        selectedStageFilter,
        selectedClientFilter,
        selectedSellerFilter,
        currentUser
    ) { flowArray ->
        @Suppress("UNCHECKED_CAST")
        val orders = flowArray[0] as List<ServiceOrder>
        val query = flowArray[1] as String
        val status = flowArray[2] as String?
        val stage = flowArray[3] as Int?
        val client = flowArray[4] as String?
        val seller = flowArray[5] as String?
        val user = flowArray[6] as User

        val isSellerRole = user.role == User.ROLE_SELLER
        val userFirstName = user.name.split(" ").firstOrNull().orEmpty()

        orders.filter { order ->
            val isSellerMatch = if (isSellerRole) {
                order.sellerName.isBlank() ||
                        order.sellerName.contains(user.name, ignoreCase = true) ||
                        user.name.contains(order.sellerName, ignoreCase = true) ||
                        (userFirstName.length >= 3 && order.sellerName.contains(userFirstName, ignoreCase = true))
            } else true

            val matchesQuery = query.isBlank() ||
                    order.osNumber.contains(query, ignoreCase = true) ||
                    order.clientName.contains(query, ignoreCase = true) ||
                    order.sellerName.contains(query, ignoreCase = true) ||
                    order.serviceDescription.contains(query, ignoreCase = true)

            val matchesStatus = status == null || order.status.equals(status, ignoreCase = true)
            val matchesStage = stage == null || order.currentStageIndex == stage
            val matchesClient = client == null || order.clientName.equals(client, ignoreCase = true)
            val matchesSeller = seller == null || order.sellerName.contains(seller, ignoreCase = true)

            isSellerMatch && matchesQuery && matchesStatus && matchesStage && matchesClient && matchesSeller
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected order flow for details screen
    val selectedOrder: StateFlow<ServiceOrder?> = combine(
        rawOrders,
        selectedOsNumber
    ) { orders, osNum ->
        if (osNum == null) null else orders.find { it.osNumber == osNum }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Selected order history logs
    val selectedOrderHistory: StateFlow<List<StageHistory>> = combine(
        selectedOsNumber,
        repository.allHistoryLogs
    ) { osNum, history ->
        if (osNum == null) emptyList() else history.filter { it.osNumber == osNum }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dashboard metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        rawOrders,
        currentUser
    ) { orders, user ->
        val isSellerRole = user.role == User.ROLE_SELLER
        val userFirstName = user.name.split(" ").firstOrNull().orEmpty()

        val roleOrders = if (isSellerRole) {
            orders.filter { order ->
                order.sellerName.isBlank() ||
                        order.sellerName.contains(user.name, ignoreCase = true) ||
                        user.name.contains(order.sellerName, ignoreCase = true) ||
                        (userFirstName.length >= 3 && order.sellerName.contains(userFirstName, ignoreCase = true))
            }
        } else {
            orders
        }

        DashboardMetrics(
            totalOrders = roleOrders.size,
            inProductionCount = roleOrders.count { it.status == ServiceOrder.STATUS_IN_PRODUCTION || (it.currentStageIndex in 1..5 && it.status != ServiceOrder.STATUS_DELAYED) },
            delayedCount = roleOrders.count { it.status == ServiceOrder.STATUS_DELAYED },
            finishedCount = roleOrders.count { it.status == ServiceOrder.STATUS_FINISHED },
            dispatchedCount = roleOrders.count { it.status == ServiceOrder.STATUS_DISPATCHED || it.currentStageIndex == 6 },
            deliveredCount = roleOrders.count { it.status == ServiceOrder.STATUS_DELIVERED || it.currentStageIndex == 7 }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )

    // Notifications
    val allNotifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        selectedStatusFilter.value = if (selectedStatusFilter.value == status) null else status
    }

    fun setStageFilter(stageIndex: Int?) {
        selectedStageFilter.value = if (selectedStageFilter.value == stageIndex) null else stageIndex
    }

    fun setSellerFilter(sellerName: String?) {
        selectedSellerFilter.value = if (selectedSellerFilter.value == sellerName) null else sellerName
    }

    fun selectOrderForDetail(osNumber: String?) {
        selectedOsNumber.value = osNumber
    }

    fun openAdvanceModal(targetStageIndex: Int? = null) {
        targetStageForAdvance.value = targetStageIndex
        advanceNotes.value = ""
        advanceProducedQuantity.value = selectedOrder.value?.producedQuantity?.toString() ?: ""
        showAdvanceModal.value = true
    }

    fun closeAdvanceModal() {
        showAdvanceModal.value = false
        advanceNotes.value = ""
        advanceProducedQuantity.value = ""
        targetStageForAdvance.value = null
    }

    fun confirmStageAdvance() {
        val osNum = selectedOsNumber.value ?: return
        val qty = advanceProducedQuantity.value.trim().toIntOrNull()
        viewModelScope.launch {
            repository.advanceOrderStage(
                osNumber = osNum,
                currentUser = currentUser.value,
                targetStageIndex = targetStageForAdvance.value,
                notes = advanceNotes.value,
                producedQuantity = qty
            )
            closeAdvanceModal()
        }
    }

    fun updateProducedQuantity(osNumber: String, quantity: Int?) {
        viewModelScope.launch {
            repository.updateProducedQuantity(osNumber, quantity)
        }
    }

    fun importCsvText(text: String? = null) {
        val content = text ?: csvInputText.value
        if (content.isBlank()) return

        isImportingCsv.value = true
        viewModelScope.launch {
            val result = repository.processCsvImport(content, allowUpdateExisting = true)
            csvImportResult.value = result
            isImportingCsv.value = false
        }
    }

    fun loadSampleCsv() {
        csvInputText.value = CsvImporter.generateSampleCsv()
        importCsvText(csvInputText.value)
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun exportReport(title: String): String {
        return DataExporter.generateTextSummaryReport(title, filteredOrders.value)
    }

    fun exportCsv(): String {
        return DataExporter.generateCsvReport(filteredOrders.value)
    }
}
