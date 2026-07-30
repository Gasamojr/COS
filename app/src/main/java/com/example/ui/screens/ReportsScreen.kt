package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DEFAULT_STAGES
import com.example.data.model.ServiceOrder
import com.example.data.util.DataExporter
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.ServiceOrderViewModel

@Composable
fun ReportsScreen(
    viewModel: ServiceOrderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orders by viewModel.filteredOrders.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val selectedStage by viewModel.selectedStageFilter.collectAsState()

    var activeReportType by remember { mutableStateOf("Por Status") }

    val reportTypes = listOf("Por Status", "Por Etapa", "Por Cliente", "Por Período")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Relatórios Gerenciais",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Gere relatórios detalhados para controle de produção e exportação.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Report Type selector chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(reportTypes) { type ->
                    FilterChip(
                        selected = activeReportType == type,
                        onClick = { activeReportType = type },
                        label = { Text(type) }
                    )
                }
            }
        }

        // Sub-filters depending on active report type
        if (activeReportType == "Por Status") {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { viewModel.setStatusFilter(null) },
                            label = { Text("Todos Status") }
                        )
                    }
                    items(ServiceOrder.ALL_STATUSES) { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { viewModel.setStatusFilter(status) },
                            label = { Text(status) }
                        )
                    }
                }
            }
        } else if (activeReportType == "Por Etapa") {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStage == null,
                            onClick = { viewModel.setStageFilter(null) },
                            label = { Text("Todas Etapas") }
                        )
                    }
                    items(DEFAULT_STAGES) { stage ->
                        FilterChip(
                            selected = selectedStage == stage.id,
                            onClick = { viewModel.setStageFilter(stage.id) },
                            label = { Text("${stage.id + 1}. ${stage.name}") }
                        )
                    }
                }
            }
        }

        // Export Action Buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Opções de Exportação",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val summaryText = viewModel.exportReport(activeReportType)
                                DataExporter.shareTextReport(context, "Relatório O.S. - $activeReportType", summaryText)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar PDF / Texto", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val csvText = viewModel.exportCsv()
                                DataExporter.shareTextReport(context, "Relatório O.S. (CSV/Excel)", csvText)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar Excel/CSV", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Report Statistics Header
        item {
            Text(
                text = "Resumo do Relatório (${orders.size} O.S. listadas)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(orders, key = { it.osNumber }) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.osNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = order.clientName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Etapa: ${DEFAULT_STAGES.getOrNull(order.currentStageIndex)?.name}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(status = order.status)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Entrega: ${order.deliveryDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
