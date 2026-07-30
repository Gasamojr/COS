package com.example.data.util

import com.example.data.model.ServiceOrder

data class CsvImportResult(
    val totalProcessed: Int,
    val insertedCount: Int,
    val updatedCount: Int,
    val errorCount: Int,
    val errors: List<String>,
    val parsedOrders: List<ServiceOrder>
)

object CsvImporter {

    fun parseCsv(csvContent: String): CsvImportResult {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return CsvImportResult(
                totalProcessed = 0,
                insertedCount = 0,
                updatedCount = 0,
                errorCount = 1,
                errors = listOf("O arquivo CSV está vazio."),
                parsedOrders = emptyList()
            )
        }

        val errors = mutableListOf<String>()
        val parsedOrders = mutableListOf<ServiceOrder>()

        // Detect delimiter (semicolon or comma)
        val firstLine = lines.first()
        val delimiter = if (firstLine.contains(";")) ";" else ","

        val headers = splitCsvLine(firstLine, delimiter).map { it.trim().lowercase() }

        // Determine column indices
        var osNumberIdx = headers.indexOfFirst { it.contains("número") || it.contains("numero") || it.contains("os") || it.contains("nº") }
        var clientIdx = headers.indexOfFirst { it.contains("cliente") || it.contains("nome do cliente") }
        var sellerIdx = headers.indexOfFirst { it.contains("vendedor") || it.contains("rep") || it.contains("comercial") || it.contains("venda") }
        var descIdx = headers.indexOfFirst { it.contains("descri") || it.contains("serviço") || it.contains("servico") }
        var issueDateIdx = headers.indexOfFirst { it.contains("emissão") || it.contains("emissao") }
        var deliveryDateIdx = headers.indexOfFirst { it.contains("entrega") }

        // Fallback to position if headers match standard columns
        val hasHeader = osNumberIdx != -1 || clientIdx != -1
        val startIndex = if (hasHeader) 1 else 0

        if (!hasHeader) {
            osNumberIdx = 0
            clientIdx = 1
            sellerIdx = 2
            descIdx = 3
            issueDateIdx = 4
            deliveryDateIdx = 5
        } else {
            if (osNumberIdx == -1) osNumberIdx = 0
            if (clientIdx == -1) clientIdx = 1
            if (sellerIdx == -1) sellerIdx = -1 // optional if not in header
            if (descIdx == -1) descIdx = if (sellerIdx != -1) 3 else 2
            if (issueDateIdx == -1) issueDateIdx = if (sellerIdx != -1) 4 else 3
            if (deliveryDateIdx == -1) deliveryDateIdx = if (sellerIdx != -1) 5 else 4
        }

        for (i in startIndex until lines.size) {
            val lineNumber = i + 1
            val rawLine = lines[i]
            val columns = splitCsvLine(rawLine, delimiter)

            if (columns.size < 2) {
                errors.add("Linha $lineNumber: Número de colunas insuficiente.")
                continue
            }

            val osNumber = columns.getOrNull(osNumberIdx)?.trim().orEmpty()
            val clientName = columns.getOrNull(clientIdx)?.trim().orEmpty()
            val sellerName = if (sellerIdx != -1) columns.getOrNull(sellerIdx)?.trim().orEmpty() else ""
            val serviceDesc = columns.getOrNull(descIdx)?.trim().orEmpty()
            val issueDate = columns.getOrNull(issueDateIdx)?.trim().orEmpty().ifEmpty { "25/07/2026" }
            val deliveryDate = columns.getOrNull(deliveryDateIdx)?.trim().orEmpty().ifEmpty { "05/08/2026" }

            if (osNumber.isBlank()) {
                errors.add("Linha $lineNumber: Número da O.S. está em branco.")
                continue
            }

            if (clientName.isBlank()) {
                errors.add("Linha $lineNumber (O.S. $osNumber): Nome do cliente está em branco.")
                continue
            }

            val order = ServiceOrder(
                osNumber = osNumber,
                clientName = clientName,
                sellerName = sellerName,
                serviceDescription = serviceDesc.ifEmpty { "Impressão Geral Gráfica" },
                issueDate = issueDate,
                deliveryDate = deliveryDate,
                currentStageIndex = 0,
                status = ServiceOrder.STATUS_AWAITING,
                updatedAt = System.currentTimeMillis()
            )

            parsedOrders.add(order)
        }

        return CsvImportResult(
            totalProcessed = parsedOrders.size + errors.size,
            insertedCount = 0, // Will be calculated after DB collision check
            updatedCount = 0,  // Will be calculated after DB collision check
            errorCount = errors.size,
            errors = errors,
            parsedOrders = parsedOrders
        )
    }

    private fun splitCsvLine(line: String, delimiter: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            if (ch == '"') {
                inQuotes = !inQuotes
            } else if (ch.toString() == delimiter && !inQuotes) {
                result.add(cur.toString().trim().removePrefix("\"").removeSuffix("\""))
                cur = StringBuilder()
            } else {
                cur.append(ch)
            }
        }
        result.add(cur.toString().trim().removePrefix("\"").removeSuffix("\""))
        return result
    }

    fun generateSampleCsv(): String {
        return """
Número da O.S.;Nome do cliente;Vendedor;Descrição do serviço;Data de emissão;Data de entrega
OS-2026-0148;Gráfica Amazonas;Roberto Mendes;Impressão de Catálogos de Produtos (5000 un);25/07/2026;05/08/2026
OS-2026-0147;Papel & Cia Ltda;Carla Antunes;Cartazes Promocionais Couché 170g (2000 un);24/07/2026;02/08/2026
OS-2026-0146;Loja do Brinde;Rodrigo Lima;Folders Tri-fold Verniz Localizado (3000 un);24/07/2026;30/07/2026
OS-2026-0145;Mega Marketing;Juliana Costa;Banners Lona 440g Ilhós (15 un);23/07/2026;31/07/2026
OS-2026-0144;Editora Alvorada;Roberto Mendes;Livros Capa Dura com Laminação Fosca (1000 un);22/07/2026;10/08/2026
OS-2026-0143;Restaurante Sabor;Carla Antunes;Cardápios Plastificados Polaseal (100 un);22/07/2026;28/07/2026
OS-2026-0142;Agência Criativa;Fernando Soares;Caixas Embalagem Cartão Duplex (4000 un);20/07/2026;08/08/2026
OS-2026-0141;Farmácia Saúde;Juliana Costa;Rótulos Adesivos Papel Couche (10000 un);20/07/2026;27/07/2026
        """.trimIndent()
    }
}
