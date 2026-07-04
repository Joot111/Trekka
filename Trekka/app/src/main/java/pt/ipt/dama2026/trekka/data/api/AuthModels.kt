package pt.ipt.dama2026.trekka.data.api

/**
 * Representa um utilizador no sistema.
 * Utilizado tanto para o processo de registo como para receber os dados do utilizador logado.
 */
data class User(
    val id: String? = null, // Identificador único gerado pelo MongoDB
    val name: String,       // Nome completo do utilizador
    val email: String,      // Endereço de correio eletrónico único
    val password: String? = null // Palavra-passe (apenas enviada no registo/login)
)

/**
 * Modelo de dados para o pedido de autenticação (Login).
 */
data class AuthRequest(
    val email: String,
    val password: String
)

/**
 * Representa a resposta de sucesso da API após autenticação.
 * Contém o token de segurança e os dados de perfil do utilizador.
 */
data class AuthResponse(
    val token: String,
    val user: User
)
