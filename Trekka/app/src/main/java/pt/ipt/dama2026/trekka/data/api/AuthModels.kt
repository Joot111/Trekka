package pt.ipt.dama2026.trekka.data.api

data class User(
    val id: String? = null,
    val name: String,
    val email: String,
    val password: String? = null
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: User
)
