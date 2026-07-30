package com.example.data.model

data class User(
    val id: String,
    val name: String,
    val role: String, // "Operador", "Supervisor", "Administrador", "Pré-Impressão", "Impressão", "Acabamento"
    val avatarColorHex: String = "#1E88E5",
    val email: String = "",
    val badgeNumber: String = "",
    val pin: String = "1234",
    val customPrivileges: Set<String>? = null
) {
    fun hasPrivilege(privilege: String): Boolean {
        if (customPrivileges != null) {
            return customPrivileges.contains(privilege)
        }
        return when (role) {
            ROLE_ADMIN -> true
            ROLE_SUPERVISOR -> privilege != PRIVILEGE_DELETE_OS
            ROLE_PRE_PRESS, ROLE_PRINTING, ROLE_FINISHING -> privilege == PRIVILEGE_EDIT_OS || privilege == PRIVILEGE_CREATE_OS || privilege == PRIVILEGE_APPROVE_DELIVERY
            ROLE_OPERATOR -> privilege == PRIVILEGE_EDIT_OS || privilege == PRIVILEGE_CREATE_OS
            else -> privilege == PRIVILEGE_EDIT_OS
        }
    }

    companion object {
        val ROLE_OPERATOR = "Operador"
        val ROLE_PRE_PRESS = "Pré-Impressão"
        val ROLE_PRINTING = "Impressão"
        val ROLE_FINISHING = "Acabamento"
        val ROLE_SUPERVISOR = "Supervisor"
        val ROLE_ADMIN = "Administrador"

        val PRIVILEGE_CREATE_OS = "can_create_os"
        val PRIVILEGE_EDIT_OS = "can_edit_os"
        val PRIVILEGE_DELETE_OS = "can_delete_os"
        val PRIVILEGE_IMPORT_CSV = "can_import_csv"
        val PRIVILEGE_MANAGE_USERS = "can_manage_users"
        val PRIVILEGE_EXPORT_REPORTS = "can_export_reports"
        val PRIVILEGE_APPROVE_DELIVERY = "can_approve_delivery"

        val ALL_PRIVILEGES = listOf(
            PRIVILEGE_CREATE_OS to "Criar Novas Ordens de Serviço (O.S.)",
            PRIVILEGE_EDIT_OS to "Apontar Produção e Avançar Etapas",
            PRIVILEGE_APPROVE_DELIVERY to "Aprovar Liberação e Despacho",
            PRIVILEGE_IMPORT_CSV to "Importar Lotes por CSV",
            PRIVILEGE_EXPORT_REPORTS to "Acessar e Exportar Relatórios",
            PRIVILEGE_MANAGE_USERS to "Gerenciar Operadores e Privilégios",
            PRIVILEGE_DELETE_OS to "Cancelar / Excluir O.S."
        )

        fun getDefaultPrivilegesForRole(role: String): Set<String> {
            return when (role) {
                ROLE_ADMIN -> ALL_PRIVILEGES.map { it.first }.toSet()
                ROLE_SUPERVISOR -> setOf(PRIVILEGE_CREATE_OS, PRIVILEGE_EDIT_OS, PRIVILEGE_APPROVE_DELIVERY, PRIVILEGE_IMPORT_CSV, PRIVILEGE_EXPORT_REPORTS, PRIVILEGE_MANAGE_USERS)
                ROLE_PRE_PRESS, ROLE_PRINTING, ROLE_FINISHING -> setOf(PRIVILEGE_CREATE_OS, PRIVILEGE_EDIT_OS, PRIVILEGE_APPROVE_DELIVERY)
                ROLE_OPERATOR -> setOf(PRIVILEGE_CREATE_OS, PRIVILEGE_EDIT_OS)
                else -> setOf(PRIVILEGE_EDIT_OS)
            }
        }

        val ALL_ROLES = listOf(
            ROLE_OPERATOR,
            ROLE_PRE_PRESS,
            ROLE_PRINTING,
            ROLE_FINISHING,
            ROLE_SUPERVISOR,
            ROLE_ADMIN
        )

        val SAMPLE_USERS = listOf(
            User("1", "João Silva", ROLE_OPERATOR, "#2196F3", "joao@grafica.com", "OP-101", "1234"),
            User("2", "Maria Santos", ROLE_PRE_PRESS, "#E91E63", "maria@grafica.com", "OP-102", "1234"),
            User("3", "Carlos Oliveira", ROLE_SUPERVISOR, "#4CAF50", "carlos@grafica.com", "SUP-201", "1234"),
            User("4", "Ana Rodrigues", ROLE_ADMIN, "#9C27B0", "ana@grafica.com", "ADM-001", "1234")
        )
    }
}
