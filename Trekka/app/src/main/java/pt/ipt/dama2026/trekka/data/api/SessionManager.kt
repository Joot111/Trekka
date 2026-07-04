package pt.ipt.dama2026.trekka.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("trekka_session", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
    }

    fun saveAuthToken(token: String) {
        prefs.edit { putString(USER_TOKEN, token) }
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun fetchUserId(): String? {
        return prefs.getString(USER_ID, null)
    }

    fun fetchUserName(): String? {
        return prefs.getString(USER_NAME, null)
    }

    fun saveUser(user: User) {
        prefs.edit {
            putString(USER_ID, user.id)
            putString(USER_NAME, user.name)
            putString(USER_EMAIL, user.email)
        }
    }

    fun logout() {
        prefs.edit { clear() }
    }

    fun isLoggedIn(): Boolean {
        return fetchAuthToken() != null
    }
}
