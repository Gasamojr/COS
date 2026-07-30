package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_orders")
data class ServiceOrder(
    @PrimaryKey
    val osNumber: String,
    val clientName: String,
    val sellerName: String = "",
    val serviceDescription: String,
    val issueDate: String,      // Format: DD/MM/YYYY or YYYY-MM-DD
    val deliveryDate: String,   // Format: DD/MM/YYYY or YYYY-MM-DD
    val currentStageIndex: Int = 0, // 0 to 7 matching production stages
    val status: String = STATUS_AWAITING, // Status: Aguardando, Em Produção, Finalizada, Expedida, Entregue, Atrasada
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val producedQuantity: Int? = null
) {
    fun getPlannedQuantity(): Int {
        val regexParen = Regex("""\(\s*(\d[\d\.\,]*)\s*(un|unid|unidades|pcs|pçs)?\s*\)""", RegexOption.IGNORE_CASE)
        val matchParen = regexParen.find(serviceDescription)
        if (matchParen != null) {
            val digits = matchParen.groupValues[1].replace(".", "").replace(",", "").toIntOrNull()
            if (digits != null && digits > 0) return digits
        }

        val regexUn = Regex("""(\d[\d\.\,]*)\s*(un|unid|unidades|pcs|pçs)""", RegexOption.IGNORE_CASE)
        val matchUn = regexUn.find(serviceDescription)
        if (matchUn != null) {
            val digits = matchUn.groupValues[1].replace(".", "").replace(",", "").toIntOrNull()
            if (digits != null && digits > 0) return digits
        }

        return 1000 // Default sensible fallback
    }

    companion object {
        const val STATUS_AWAITING = "Aguardando"
        const val STATUS_IN_PRODUCTION = "Em Produção"
        const val STATUS_FINISHED = "Finalizada"
        const val STATUS_DISPATCHED = "Expedida"
        const val STATUS_DELIVERED = "Entregue"
        const val STATUS_DELAYED = "Atrasada"

        val ALL_STATUSES = listOf(
            STATUS_AWAITING,
            STATUS_IN_PRODUCTION,
            STATUS_DELAYED,
            STATUS_FINISHED,
            STATUS_DISPATCHED,
            STATUS_DELIVERED
        )
    }
}

val DEFAULT_STAGES = listOf(
    ProductionStage(0, "Recebimento da O.S.", "Entrada e preparação dos arquivos gráficos"),
    ProductionStage(1, "Impressão", "Impressão em CTP / Offset / Digital"),
    ProductionStage(2, "Laminação", "Aplicação de laminação fosca/brilho ou verniz"),
    ProductionStage(3, "Corte", "Corte e vinco na guilhotina ou maquete"),
    ProductionStage(4, "Acabamento", "Dobra, grampo, espiral e intercalação"),
    ProductionStage(5, "Conferência", "Inspeção e controle de qualidade de impressão"),
    ProductionStage(6, "Expedição", "Embalagem, etiquetação e despacho"),
    ProductionStage(7, "Entrega", "Transporte e confirmação de recebimento no cliente")
)

data class ProductionStage(
    val id: Int,
    val name: String,
    val description: String
)
