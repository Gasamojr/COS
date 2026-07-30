package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DEFAULT_STAGES
import com.example.data.model.ServiceOrder
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusAwaiting
import com.example.ui.theme.StatusDelayed
import com.example.ui.theme.StatusDelivered
import com.example.ui.theme.StatusDispatched
import com.example.ui.theme.StatusFinished
import com.example.ui.theme.StatusInProduction
import com.example.ui.viewmodel.ServiceOrderViewModel

@Composable
fun DashboardScreen(
    viewModel: ServiceOrderViewModel,
    onNavigateToOsList: (String?) -> Unit,
    onNavigateToOsDetail: (String) -> Unit,
    onNavigateToImportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val orders by viewModel.filteredOrders.collectAsState()
    val recentOrders = orders.take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Painel de Controle",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Acompanhamento de Produção Gráfica em Tempo Real",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onNavigateToImportCsv,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("dashboard_import_csv_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Importar CSV",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importar CSV", fontSize = 12.sp)
                }
            }
        }

        // Real-Time Indicator Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total de O.S.",
                        count = metrics.totalOrders,
                        icon = Icons.Default.Assignment,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToOsList(null) }
                    )
                    MetricCard(
                        title = "Em Produção",
                        count = metrics.inProductionCount,
                        icon = Icons.Default.PrecisionManufacturing,
                        color = StatusInProduction,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToOsList(ServiceOrder.STATUS_IN_PRODUCTION) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "O.S. Atrasadas",
                        count = metrics.delayedCount,
                        icon = Icons.Default.Warning,
                        color = StatusDelayed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToOsList(ServiceOrder.STATUS_DELAYED) }
                    )
                    MetricCard(
                        title = "Finalizadas",
                        count = metrics.finishedCount,
                        icon = Icons.Default.CheckCircle,
                        color = StatusFinished,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToOsList(ServiceOrder.STATUS_FINISHED) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Expedidas",
                        count = metrics.dispatchedCount,
                        icon = Icons.Default.LocalShipping,
                        color = StatusDispatched,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToOsList(ServiceOrder.STATUS_DISPATCHED) }
                    )
                    MetricCard(
                        title = "Entregues",
                        count = metrics.deliveredCount,
                        icon = Icons.Default.CheckCircle,
                        color = StatusDelivered,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToOsList(ServiceOrder.STATUS_DELIVERED) }
                    )
                }
            }
        }

        // Visual Status Breakdown Card
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
                        text = "Distribuição por Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val total = if (metrics.totalOrders == 0) 1 else metrics.totalOrders

                    // Progress bar visual breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        if (metrics.inProductionCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(metrics.inProductionCount.toFloat() / total)
                                    .fillMaxSize()
                                    .background(StatusInProduction)
                            )
                        }
                        if (metrics.delayedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(metrics.delayedCount.toFloat() / total)
                                    .fillMaxSize()
                                    .background(StatusDelayed)
                            )
                        }
                        if (metrics.finishedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(metrics.finishedCount.toFloat() / total)
                                    .fillMaxSize()
                                    .background(StatusFinished)
                            )
                        }
                        if (metrics.dispatchedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(metrics.dispatchedCount.toFloat() / total)
                                    .fillMaxSize()
                                    .background(StatusDispatched)
                            )
                        }
                        if (metrics.deliveredCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(metrics.deliveredCount.toFloat() / total)
                                    .fillMaxSize()
                                    .background(StatusDelivered)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusLegendItem("Em Produção", metrics.inProductionCount, total, StatusInProduction)
                        StatusLegendItem("Atrasadas", metrics.delayedCount, total, StatusDelayed)
                        StatusLegendItem("Finalizadas", metrics.finishedCount, total, StatusFinished)
                        StatusLegendItem("Expedidas", metrics.dispatchedCount, total, StatusDispatched)
                        StatusLegendItem("Entregues", metrics.deliveredCount, total, StatusDelivered)
                    }
                }
            }
        }

        // Recent Orders List Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "O.S. Recentes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Ver Todas",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToOsList(null) }
                )
            }
        }

        items(recentOrders, key = { it.osNumber }) { order ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToOsDetail(order.osNumber) }
                    .testTag("os_card_${order.osNumber}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.osNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatusBadge(status = order.status)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = order.clientName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = order.serviceDescription,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Etapa: ${DEFAULT_STAGES.getOrNull(order.currentStageIndex)?.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Entrega: ${order.deliveryDate}",
                            fontSize = 12.sp,
                            color = if (order.status == ServiceOrder.STATUS_DELAYED) StatusDelayed else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (order.status == ServiceOrder.STATUS_DELAYED) FontWeight.Bold else FontWeight.Normal
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

@Composable
private fun StatusLegendItem(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = (count.toFloat() / total * 100).toInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "$count ($percentage%)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
