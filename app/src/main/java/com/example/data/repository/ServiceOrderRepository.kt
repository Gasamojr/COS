package com.example.data.repository

import com.example.data.dao.NotificationDao
import com.example.data.dao.ServiceOrderDao
import com.example.data.dao.StageHistoryDao
import com.example.data.model.AppNotification
import com.example.data.model.DEFAULT_STAGES
import com.example.data.model.ServiceOrder
import com.example.data.model.StageHistory
import com.example.data.model.User
import com.example.data.util.CsvImportResult
import com.example.data.util.CsvImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ServiceOrderRepository(
    private val serviceOrderDao: ServiceOrderDao,
    private val stageHistoryDao: StageHistoryDao,
    private val notificationDao: NotificationDao
) {
    val allOrders: Flow<List<ServiceOrder>> = serviceOrderDao.getAllOrders()
    val allNotifications: Flow<List<AppNotification>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()
    val allHistoryLogs: Flow<List<StageHistory>> = stageHistoryDao.getAllRecentHistory()

    fun getOrderFlowByNumber(osNumber: String): Flow<ServiceOrder?> {
        return serviceOrderDao.getOrderFlowByNumber(osNumber)
    }

    fun getHistoryForOrder(osNumber: String): Flow<List<StageHistory>> {
        return stageHistoryDao.getHistoryForOrder(osNumber)
    }

    suspend fun getOrderByNumber(osNumber: String): ServiceOrder? {
        return serviceOrderDao.getOrderByNumber(osNumber)
    }

    suspend fun advanceOrderStage(
        osNumber: String,
        currentUser: User,
        targetStageIndex: Int? = null,
        notes: String = "",
        producedQuantity: Int? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val order = serviceOrderDao.getOrderByNumber(osNumber) ?: return@withContext false
        val currentStage = order.currentStageIndex
        val nextStageIndex = targetStageIndex ?: (currentStage + 1)

        if (nextStageIndex < 0 || nextStageIndex >= DEFAULT_STAGES.size) return@withContext false

        val fromStageName = DEFAULT_STAGES.getOrNull(currentStage)?.name ?: "Inicial"
        val toStageName = DEFAULT_STAGES[nextStageIndex].name

        // Determine updated status automatically
        val newStatus = when (nextStageIndex) {
            0 -> ServiceOrder.STATUS_AWAITING
            in 1..5 -> ServiceOrder.STATUS_IN_PRODUCTION
            6 -> ServiceOrder.STATUS_DISPATCHED
            7 -> ServiceOrder.STATUS_DELIVERED
            else -> ServiceOrder.STATUS_FINISHED
        }

        val updatedQuantity = producedQuantity ?: order.producedQuantity

        val updatedOrder = order.copy(
            currentStageIndex = nextStageIndex,
            status = newStatus,
            updatedAt = System.currentTimeMillis(),
            notes = if (notes.isNotBlank()) notes else order.notes,
            producedQuantity = updatedQuantity
        )

        serviceOrderDao.updateOrder(updatedOrder)

        // Record history log
        val quantityNote = if (producedQuantity != null) " [Qtd. Produzida: $producedQuantity un]" else ""
        val historyEntry = StageHistory(
            osNumber = osNumber,
            fromStage = fromStageName,
            toStage = toStageName,
            user = "${currentUser.name} (${currentUser.role})",
            timestamp = System.currentTimeMillis(),
            notes = (notes + quantityNote).trim()
        )
        stageHistoryDao.insertHistory(historyEntry)

        // Insert notification
        val notification = AppNotification(
            title = "Avanço de Etapa - O.S. $osNumber",
            message = "Movimentada de '$fromStageName' para '$toStageName' por ${currentUser.name}.${if (producedQuantity != null) " Quantidade conferida: $producedQuantity un." else ""}",
            type = AppNotification.TYPE_STAGE_CHANGE,
            osNumber = osNumber
        )
        notificationDao.insertNotification(notification)

        true
    }

    suspend fun updateProducedQuantity(osNumber: String, quantity: Int?): Boolean = withContext(Dispatchers.IO) {
        val order = serviceOrderDao.getOrderByNumber(osNumber) ?: return@withContext false
        val updated = order.copy(producedQuantity = quantity, updatedAt = System.currentTimeMillis())
        serviceOrderDao.updateOrder(updated)
        true
    }

    suspend fun processCsvImport(
        csvText: String,
        allowUpdateExisting: Boolean = true
    ): CsvImportResult = withContext(Dispatchers.IO) {
        val parsedResult = CsvImporter.parseCsv(csvText)
        if (parsedResult.parsedOrders.isEmpty()) {
            if (parsedResult.errors.isNotEmpty()) {
                notificationDao.insertNotification(
                    AppNotification(
                        title = "Erro na Importação CSV",
                        message = "Falha ao importar CSV: ${parsedResult.errors.firstOrNull()}",
                        type = AppNotification.TYPE_IMPORT_RESULT
                    )
                )
            }
            return@withContext parsedResult
        }

        var insertedCount = 0
        var updatedCount = 0

        parsedResult.parsedOrders.forEach { incoming ->
            val existing = serviceOrderDao.getOrderByNumber(incoming.osNumber)
            if (existing != null) {
                if (allowUpdateExisting) {
                    val updated = existing.copy(
                        clientName = incoming.clientName,
                        sellerName = incoming.sellerName.ifBlank { existing.sellerName },
                        serviceDescription = incoming.serviceDescription,
                        issueDate = incoming.issueDate,
                        deliveryDate = incoming.deliveryDate,
                        updatedAt = System.currentTimeMillis()
                    )
                    serviceOrderDao.updateOrder(updated)
                    updatedCount++
                }
            } else {
                serviceOrderDao.insertOrder(incoming)
                insertedCount++

                // Record initial history
                stageHistoryDao.insertHistory(
                    StageHistory(
                        osNumber = incoming.osNumber,
                        fromStage = "Sistema",
                        toStage = DEFAULT_STAGES[0].name,
                        user = "Importador CSV",
                        timestamp = System.currentTimeMillis(),
                        notes = "O.S. importada via CSV"
                    )
                )
            }
        }

        val totalImported = insertedCount + updatedCount
        val finalResult = parsedResult.copy(
            insertedCount = insertedCount,
            updatedCount = updatedCount
        )

        notificationDao.insertNotification(
            AppNotification(
                title = "Importação CSV Concluída",
                message = "Importação finalizada com sucesso! $insertedCount novas O.S. criadas, $updatedCount atualizadas.",
                type = AppNotification.TYPE_IMPORT_RESULT
            )
        )

        finalResult
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun checkSeedData() = withContext(Dispatchers.IO) {
        if (serviceOrderDao.countOrders() == 0) {
            populateInitialSeedData()
        }
    }

    private suspend fun populateInitialSeedData() {
        val sampleCsv = CsvImporter.generateSampleCsv()
        processCsvImport(sampleCsv, allowUpdateExisting = true)

        // Set realistic stages for sample orders so the UI opens with rich graphic shop activity
        advanceOrderStage("OS-2026-0148", User.SAMPLE_USERS[1], targetStageIndex = 1, notes = "Impressão iniciada na máquina CTP Offset HD")
        advanceOrderStage("OS-2026-0147", User.SAMPLE_USERS[0], targetStageIndex = 2, notes = "Laminação fosca concluída")
        advanceOrderStage("OS-2026-0146", User.SAMPLE_USERS[2], targetStageIndex = 5, notes = "Conferência de corte e dobra efetuada", producedQuantity = 3000)
        advanceOrderStage("OS-2026-0145", User.SAMPLE_USERS[1], targetStageIndex = 6, notes = "Embalado e pronto na Expedição", producedQuantity = 15)
        advanceOrderStage("OS-2026-0141", User.SAMPLE_USERS[3], targetStageIndex = 7, notes = "Entregue e assinado pelo cliente", producedQuantity = 10000)

        // Mark OS-2026-0147 as delayed for demonstration
        val delayed = serviceOrderDao.getOrderByNumber("OS-2026-0147")
        if (delayed != null) {
            serviceOrderDao.updateOrder(delayed.copy(status = ServiceOrder.STATUS_DELAYED))
        }

        notificationDao.insertNotification(
            AppNotification(
                title = "Alerta de Prazo - O.S. OS-2026-0147",
                message = "A O.S. OS-2026-0147 do cliente Papel & Cia está com prazo próximo/atrasado!",
                type = AppNotification.TYPE_OVERDUE,
                osNumber = "OS-2026-0147"
            )
        )
    }
}
