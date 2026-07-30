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

    // Active User State
    val currentUser = MutableStateFlow(User.SAMPLE_USERS[0]) // João Silva (Operador)

    fun setCurrentUser(user: User) {
        currentUser.value = user
    }

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedStageFilter = MutableStateFlow<Int?>(null)
    val selectedClientFilter = MutableStateFlow<String?>(null)

    // CSV Import State
    val csvInputText = MutableStateFlow("")
    val csvImportResult = MutableStateFlow<CsvImportResult?>(null)
    val isImportingCsv = MutableStateFlow(false)

    // Selection & Navigation
    val selectedOsNumber = MutableStateFlow<String?>(null)
    val showAdvanceModal = MutableStateFlow(false)
    val advanceNotes = MutableStateFlow("")
    val targetStageForAdvance = MutableStateFlow<Int?>(null)

    // UI Dialogs
    val showNotificationSheet = MutableStateFlow(false)
    val showUserRoleMenu = MutableStateFlow(false)

    // Flow for raw orders
    private val rawOrders = repository.allOrders

    // Filtered orders list
    val filteredOrders: StateFlow<List<ServiceOrder>> = combine(
        rawOrders,
        searchQuery,
        selectedStatusFilter,
        selectedStageFilter,
        selectedClientFilter
    ) { orders, query, status, stage, client ->
        orders.filter { order ->
            val matchesQuery = query.isBlank() ||
                    order.osNumber.contains(query, ignoreCase = true) ||
                    order.clientName.contains(query, ignoreCase = true) ||
                    order.serviceDescription.contains(query, ignoreCase = true)

            val matchesStatus = status == null || order.status.equals(status, ignoreCase = true)
            val matchesStage = stage == null || order.currentStageIndex == stage
            val matchesClient = client == null || order.clientName.equals(client, ignoreCase = true)

            matchesQuery && matchesStatus && matchesStage && matchesClient
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
    val dashboardMetrics: StateFlow<DashboardMetrics> = rawOrders.combine(
        MutableStateFlow(Unit)
    ) { orders, _ ->
        DashboardMetrics(
            totalOrders = orders.size,
            inProductionCount = orders.count { it.status == ServiceOrder.STATUS_IN_PRODUCTION || (it.currentStageIndex in 1..5 && it.status != ServiceOrder.STATUS_DELAYED) },
            delayedCount = orders.count { it.status == ServiceOrder.STATUS_DELAYED },
            finishedCount = orders.count { it.status == ServiceOrder.STATUS_FINISHED },
            dispatchedCount = orders.count { it.status == ServiceOrder.STATUS_DISPATCHED || it.currentStageIndex == 6 },
            deliveredCount = orders.count { it.status == ServiceOrder.STATUS_DELIVERED || it.currentStageIndex == 7 }
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

    fun selectOrderForDetail(osNumber: String?) {
        selectedOsNumber.value = osNumber
    }

    fun openAdvanceModal(targetStageIndex: Int? = null) {
        targetStageForAdvance.value = targetStageIndex
        advanceNotes.value = ""
        showAdvanceModal.value = true
    }

    fun closeAdvanceModal() {
        showAdvanceModal.value = false
        advanceNotes.value = ""
        targetStageForAdvance.value = null
    }

    fun confirmStageAdvance() {
        val osNum = selectedOsNumber.value ?: return
        viewModelScope.launch {
            repository.advanceOrderStage(
                osNumber = osNum,
                currentUser = currentUser.value,
                targetStageIndex = targetStageForAdvance.value,
                notes = advanceNotes.value
            )
            closeAdvanceModal()
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
