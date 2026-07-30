package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DEFAULT_STAGES
import com.example.data.model.ServiceOrder
import com.example.data.model.User
import com.example.ui.theme.StatusAwaiting
import com.example.ui.theme.StatusDelayed
import com.example.ui.theme.StatusDelivered
import com.example.ui.theme.StatusDispatched
import com.example.ui.theme.StatusFinished
import com.example.ui.theme.StatusInProduction

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        ServiceOrder.STATUS_AWAITING -> StatusAwaiting.copy(alpha = 0.15f) to StatusAwaiting
        ServiceOrder.STATUS_IN_PRODUCTION -> StatusInProduction.copy(alpha = 0.15f) to StatusInProduction
        ServiceOrder.STATUS_DELAYED -> StatusDelayed.copy(alpha = 0.15f) to StatusDelayed
        ServiceOrder.STATUS_FINISHED -> StatusFinished.copy(alpha = 0.15f) to StatusFinished
        ServiceOrder.STATUS_DISPATCHED -> StatusDispatched.copy(alpha = 0.15f) to StatusDispatched
        ServiceOrder.STATUS_DELIVERED -> StatusDelivered.copy(alpha = 0.15f) to StatusDelivered
        else -> Color.Gray.copy(alpha = 0.15f) to Color.DarkGray
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TopUserHeader(
    currentUser: User,
    unreadNotificationsCount: Int,
    onUserSwitchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { userMenuExpanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuário",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = currentUser.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentUser.role,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = userMenuExpanded,
                onDismissRequest = { userMenuExpanded = false }
            ) {
                User.SAMPLE_USERS.forEach { user ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(user.name, fontWeight = FontWeight.SemiBold)
                                Text(user.role, fontSize = 12.sp, color = Color.Gray)
                            }
                        },
                        onClick = {
                            userMenuExpanded = false
                            onUserSwitchClick()
                        }
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.testTag("notification_bell_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge {
                                Text(
                                    text = if (unreadNotificationsCount > 9) "9+" else unreadNotificationsCount.toString()
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificações",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val bg = containerColor ?: MaterialTheme.colorScheme.surface
    val isColoredBg = containerColor != null && containerColor != MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = if (isColoredBg) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = count.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StageStepperHorizontal(
    currentStageIndex: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Etapa Atual (${currentStageIndex + 1}/8)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = DEFAULT_STAGES.getOrNull(currentStageIndex)?.name ?: "N/A",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            DEFAULT_STAGES.forEachIndexed { index, _ ->
                val isCompleted = index < currentStageIndex
                val isCurrent = index == currentStageIndex
                val color = when {
                    isCompleted -> StatusDelivered
                    isCurrent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun AdvanceStageDialog(
    osNumber: String,
    currentStageIndex: Int,
    targetStageIndex: Int?,
    notes: String,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val nextIndex = targetStageIndex ?: (currentStageIndex + 1)
    val nextStage = DEFAULT_STAGES.getOrNull(nextIndex)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Avançar Etapa da O.S.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "O.S. número: $osNumber",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Etapa Atual",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = DEFAULT_STAGES.getOrNull(currentStageIndex)?.name ?: "N/A",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Próxima Etapa",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = nextStage?.name ?: "Concluído",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = StatusDelivered
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Observações (opcional)") },
                    placeholder = { Text("Ex: Impressão ajustada, sem defeitos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_advance_stage_button")
                    ) {
                        Text("Confirmar Avanço")
                    }
                }
            }
        }
    }
}
