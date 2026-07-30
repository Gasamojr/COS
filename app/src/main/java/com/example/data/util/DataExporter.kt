package com.example.data.util

import android.content.Context
import android.content.Intent
import com.example.data.model.DEFAULT_STAGES
import com.example.data.model.ServiceOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExporter {

    fun generateCsvReport(orders: List<ServiceOrder>): String {
        val sb = StringBuilder()
        sb.append("Número O.S.;Cliente;Vendedor;Descrição;Data Emissão;Data Entrega;Etapa Atual;Status\n")
        orders.forEach { order ->
            val stageName = DEFAULT_STAGES.getOrNull(order.currentStageIndex)?.name ?: "Desconhecida"
            sb.append("${order.osNumber};\"${order.clientName}\";\"${order.sellerName}\";\"${order.serviceDescription}\";${order.issueDate};${order.deliveryDate};\"$stageName\";${order.status}\n")
        }
        return sb.toString()
    }

    fun generateTextSummaryReport(
        title: String,
        orders: List<ServiceOrder>
    ): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("RELATÓRIO - CONTROLE DE O.S. (INDÚSTRIA GRÁFICA)\n")
        sb.append("Tipo: $title\n")
        sb.append("Gerado em: ${sdf.format(Date())}\n")
        sb.append("Total de O.S.: ${orders.size}\n")
        sb.append("=========================================\n\n")

        val statusCounts = orders.groupBy { it.status }
        sb.append("RESUMO POR STATUS:\n")
        statusCounts.forEach { (status, list) ->
            sb.append("- $status: ${list.size}\n")
        }
        sb.append("\nLISTAGEM DETALHADA:\n")
        sb.append("-----------------------------------------\n")

        orders.forEachIndexed { index, order ->
            val stageName = DEFAULT_STAGES.getOrNull(order.currentStageIndex)?.name ?: "N/A"
            val sellerInfo = if (order.sellerName.isNotBlank()) " | Vendedor: ${order.sellerName}" else ""
            sb.append("${index + 1}. O.S.: ${order.osNumber}\n")
            sb.append("   Cliente: ${order.clientName}$sellerInfo\n")
            sb.append("   Descrição: ${order.serviceDescription}\n")
            sb.append("   Emissão: ${order.issueDate} | Entrega: ${order.deliveryDate}\n")
            sb.append("   Etapa: $stageName (${order.currentStageIndex + 1}/8)\n")
            sb.append("   Status: ${order.status}\n")
            sb.append("-----------------------------------------\n")
        }

        return sb.toString()
    }

    fun shareTextReport(context: Context, reportTitle: String, content: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, reportTitle)
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Exportar Relatório - $reportTitle")
        context.startActivity(shareIntent)
    }
}
