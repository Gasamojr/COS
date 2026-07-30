package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.ui.components.StageStepperHorizontal
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDelayed
import com.example.ui.viewmodel.ServiceOrderViewModel

@Composable
fun ServiceOrderListScreen(
    viewModel: ServiceOrderViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.filteredOrders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val selectedStage by viewModel.selectedStageFilter.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ordens de Serviço",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Buscar por N° O.S., Cliente ou Descrição...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("os_search_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { viewModel.setStatusFilter(null) },
                    label = { Text("Todos") },
                    modifier = Modifier.testTag("filter_status_all")
                )
            }

            items(ServiceOrder.ALL_STATUSES) { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { viewModel.setStatusFilter(status) },
                    label = { Text(status) },
                    modifier = Modifier.testTag("filter_status_$status")
                )
            }
        }

        // Stage Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(
                    text = "Etapa:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp, end = 4.dp)
                )
            }

            items(DEFAULT_STAGES) { stage ->
                val isSelected = selectedStage == stage.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setStageFilter(stage.id) },
                    label = { Text("${stage.id + 1}. ${stage.name}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${orders.size} O.S. encontradas",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhuma O.S. encontrada para os filtros selecionados.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders, key = { it.osNumber }) { order ->
                    ServiceOrderListItemCard(
                        order = order,
                        onCardClick = { onNavigateToDetail(order.osNumber) },
                        onQuickAdvanceClick = {
                            viewModel.selectOrderForDetail(order.osNumber)
                            viewModel.openAdvanceModal()
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ServiceOrderListItemCard(
    order: ServiceOrder,
    onCardClick: () -> Unit,
    onQuickAdvanceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("os_list_item_${order.osNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.osNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = order.clientName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = order.serviceDescription,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            StageStepperHorizontal(currentStageIndex = order.currentStageIndex)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Emissão: ${order.issueDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Entrega: ${order.deliveryDate}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.status == ServiceOrder.STATUS_DELAYED) StatusDelayed else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (order.currentStageIndex < DEFAULT_STAGES.size - 1) {
                    Button(
                        onClick = onQuickAdvanceClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("quick_advance_button_${order.osNumber}")
                    ) {
                        Text("Avançar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Avançar Etapa",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
