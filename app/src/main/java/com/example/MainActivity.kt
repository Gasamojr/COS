package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.AdvanceStageDialog
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.RestrictedAccessCard
import com.example.ui.components.TopUserHeader
import com.example.ui.screens.CsvImportScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ServiceOrderDetailScreen
import com.example.ui.screens.ServiceOrderListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UserRegistrationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ServiceOrderViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ServiceOrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel = viewModel)
            }
        }
    }
}

data class NavTabItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainAppLayout(viewModel: ServiceOrderViewModel) {
    var currentRoute by remember { mutableStateOf("dashboard") }

    val currentUser by viewModel.currentUser.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()

    val showAdvanceModal by viewModel.showAdvanceModal.collectAsState()
    val advanceNotes by viewModel.advanceNotes.collectAsState()
    val advanceProducedQuantity by viewModel.advanceProducedQuantity.collectAsState()
    val targetStageForAdvance by viewModel.targetStageForAdvance.collectAsState()

    var showNotificationsSheet by remember { mutableStateOf(false) }

    val allNavItems = listOf(
        NavTabItem("dashboard", "Painel", Icons.Default.Speed),
        NavTabItem("os_list", "O.S.", Icons.Default.Assignment),
        NavTabItem("import_csv", "Importar", Icons.Default.FileUpload),
        NavTabItem("reports", "Relatórios", Icons.Default.Assessment),
        NavTabItem("settings", "Ajustes", Icons.Default.Settings)
    )

    val navItems = allNavItems.filter { tab ->
        when (tab.route) {
            "import_csv" -> currentUser.hasPrivilege(User.PRIVILEGE_IMPORT_CSV)
            "reports" -> currentUser.hasPrivilege(User.PRIVILEGE_EXPORT_REPORTS)
            else -> true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            TopUserHeader(
                currentUser = currentUser,
                unreadNotificationsCount = unreadNotifications,
                onUserSwitchClick = { currentRoute = "login" },
                onNotificationClick = { showNotificationsSheet = true }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEach { tab ->
                    val isSelected = currentRoute == tab.route || (currentRoute == "os_detail" && tab.route == "os_list")
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentRoute = tab.route },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_${tab.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                "dashboard" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToOsList = { statusFilter ->
                        viewModel.setStatusFilter(statusFilter)
                        currentRoute = "os_list"
                    },
                    onNavigateToOsDetail = { osNumber ->
                        viewModel.selectOrderForDetail(osNumber)
                        currentRoute = "os_detail"
                    },
                    onNavigateToImportCsv = {
                        if (currentUser.hasPrivilege(User.PRIVILEGE_IMPORT_CSV)) {
                            currentRoute = "import_csv"
                        }
                    }
                )

                "os_list" -> ServiceOrderListScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { osNumber ->
                        viewModel.selectOrderForDetail(osNumber)
                        currentRoute = "os_detail"
                    }
                )

                "os_detail" -> ServiceOrderDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { currentRoute = "os_list" }
                )

                "import_csv" -> {
                    if (currentUser.hasPrivilege(User.PRIVILEGE_IMPORT_CSV)) {
                        CsvImportScreen(viewModel = viewModel)
                    } else {
                        RestrictedAccessCard(
                            title = "Importação de CSV Indisponível",
                            message = "Seu perfil (${currentUser.role}) não possui permissão para importar arquivos CSV. Solicite ao Administrador ou Supervisor."
                        )
                    }
                }

                "reports" -> {
                    if (currentUser.hasPrivilege(User.PRIVILEGE_EXPORT_REPORTS)) {
                        ReportsScreen(viewModel = viewModel)
                    } else {
                        RestrictedAccessCard(
                            title = "Relatórios Indisponíveis",
                            message = "Seu perfil (${currentUser.role}) não possui privilégio de exportar e visualizar relatórios."
                        )
                    }
                }

                "settings" -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { currentRoute = "login" },
                    onNavigateToRegister = {
                        if (currentUser.hasPrivilege(User.PRIVILEGE_MANAGE_USERS)) {
                            currentRoute = "register_user"
                        }
                    }
                )

                "login" -> LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { currentRoute = "dashboard" },
                    onRegisterClick = {
                        if (currentUser.hasPrivilege(User.PRIVILEGE_MANAGE_USERS)) {
                            currentRoute = "register_user"
                        }
                    }
                )

                "register_user" -> {
                    if (currentUser.hasPrivilege(User.PRIVILEGE_MANAGE_USERS)) {
                        UserRegistrationScreen(
                            viewModel = viewModel,
                            onBackClick = { currentRoute = "login" },
                            onRegistrationSuccess = { currentRoute = "dashboard" }
                        )
                    } else {
                        RestrictedAccessCard(
                            title = "Cadastro de Usuários Restrito",
                            message = "Apenas administradores e supervisores com permissão de gerenciamento de usuários podem cadastrar novos operadores."
                        )
                    }
                }
            }
        }

        // Advance Stage Modal
        if (showAdvanceModal && selectedOrder != null) {
            AdvanceStageDialog(
                osNumber = selectedOrder!!.osNumber,
                currentStageIndex = selectedOrder!!.currentStageIndex,
                targetStageIndex = targetStageForAdvance,
                notes = advanceNotes,
                producedQuantityInput = advanceProducedQuantity,
                plannedQuantity = selectedOrder!!.getPlannedQuantity(),
                onNotesChange = { viewModel.advanceNotes.value = it },
                onProducedQuantityChange = { viewModel.advanceProducedQuantity.value = it },
                onDismiss = { viewModel.closeAdvanceModal() },
                onConfirm = { viewModel.confirmStageAdvance() }
            )
        }

        // Notifications Bottom Sheet
        if (showNotificationsSheet) {
            NotificationsSheet(
                notifications = notifications,
                onDismiss = { showNotificationsSheet = false },
                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                onNotificationClick = { osNum ->
                    showNotificationsSheet = false
                    if (osNum != null) {
                        viewModel.selectOrderForDetail(osNum)
                        currentRoute = "os_detail"
                    }
                }
            )
        }
    }
}
