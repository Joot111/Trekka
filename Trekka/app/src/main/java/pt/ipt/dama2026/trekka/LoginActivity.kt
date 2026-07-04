package pt.ipt.dama2026.trekka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.data.api.AuthRequest
import pt.ipt.dama2026.trekka.data.api.RetrofitClient
import pt.ipt.dama2026.trekka.data.api.SessionManager

/**
 * Atividade de Autenticação inicial.
 * Gere o acesso do utilizador à aplicação através de validação de credenciais via API REST.
 * Implementa validações visuais robustas para campos vazios e formatos de email.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        // Se já existir uma sessão ativa, redireciona automaticamente para o ecrã principal
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Mapeamento de componentes da interface
        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmail)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val layoutPassword = findViewById<TextInputLayout>(R.id.layoutPassword)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val languageBox = findViewById<TextView>(R.id.languageBox)
        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)

        // Remove mensagens de erro assim que o utilizador começa a corrigir o campo
        editEmail.doOnTextChanged { _, _, _, _ -> layoutEmail.error = null }
        editPassword.doOnTextChanged { _, _, _, _ -> layoutPassword.error = null }

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val pass = editPassword.text.toString().trim()

            // Validação de entrada de dados (UI/UX e Acessibilidade)
            var hasError = false
            if (email.isBlank()) {
                layoutEmail.error = getString(R.string.error_empty_fields)
                hasError = true
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = getString(R.string.invalid_email)
                hasError = true
            }

            if (pass.isBlank()) {
                layoutPassword.error = getString(R.string.error_empty_fields)
                hasError = true
            } else if (pass.length < 6) {
                layoutPassword.error = getString(R.string.invalid_password)
                hasError = true
            }

            if (hasError) return@setOnClickListener

            // Início do processo de autenticação via rede
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                try {
                    // Chamada à API REST no Render
                    val response = RetrofitClient.instance.login(AuthRequest(email, pass))
                    
                    // Salvaguarda da sessão e dados do utilizador localmente
                    sessionManager.saveAuthToken(response.token)
                    sessionManager.saveUser(response.user)
                    
                    Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } catch (_: Exception) {
                    Toast.makeText(this@LoginActivity, R.string.login_error, Toast.LENGTH_LONG).show()
                } finally {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                }
            }
        }

        // Navegação para criação de conta
        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Gestão de idioma e secção "Sobre" no ecrã de login
        languageBox.setOnClickListener {
            val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "pt"
            val newLocale = if (currentLocale == "pt") "en" else "pt"
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLocale)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
