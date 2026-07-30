package com.example.data.model

data class User(
    val id: String,
    val name: String,
    val role: String, // "Operador", "Supervisor", "Administrador"
    val avatarColorHex: String = "#1E88E5"
) {
    companion object {
        val ROLE_OPERATOR = "Operador"
        val ROLE_SUPERVISOR = "Supervisor"
        val ROLE_ADMIN = "Administrador"

        val SAMPLE_USERS = listOf(
            User("1", "João Silva", ROLE_OPERATOR, "#2196F3"),
            User("2", "Maria Santos", ROLE_OPERATOR, "#E91E63"),
            User("3", "Carlos Oliveira", ROLE_SUPERVISOR, "#4CAF50"),
            User("4", "Ana Rodrigues", ROLE_ADMIN, "#9C27B0")
        )
    }
}
