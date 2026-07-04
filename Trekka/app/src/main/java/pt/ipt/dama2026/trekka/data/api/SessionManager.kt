package pt.ipt.dama2026.trekka.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Gestor de Sessão do utilizador.
 * Utiliza SharedPreferences para persistir localmente o token de autenticação e os dados do perfil.
 * Garante que o utilizador se mantém logado mesmo após fechar a aplicação.
 */
class SessionManager(context: Context) {
    
    // Ficheiro de preferências privado da aplicação
    private var prefs: SharedPreferences =
        context.getSharedPreferences("trekka_session", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
    }

    /**
     * Guarda o token de acesso recebido da API.
     */
    fun saveAuthToken(token: String) {
        prefs.edit { putString(USER_TOKEN, token) }
    }

    /**
     * Recupera o token de acesso atual.
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Recupera o ID único do utilizador autenticado.
     */
    fun fetchUserId(): String? {
        return prefs.getString(USER_ID, null)
    }

    /**
     * Recupera o nome do utilizador para exibição na UI.
     */
    fun fetchUserName(): String? {
        return prefs.getString(USER_NAME, null)
    }

    /**
     * Guarda os dados básicos do perfil do utilizador.
     */
    fun saveUser(user: User) {
        prefs.edit {
            putString(USER_ID, user.id)
            putString(USER_NAME, user.name)
            putString(USER_EMAIL, user.email)
        }
    }

    /**
     * Limpa todos os dados da sessão (Logout).
     */
    fun logout() {
        prefs.edit { clear() }
    }

    /**
     * Verifica se existe um utilizador autenticado no momento.
     */
    fun isLoggedIn(): Boolean {
        return fetchAuthToken() != null
    }
}
