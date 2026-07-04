package pt.ipt.dama2026.trekka

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.data.api.RetrofitClient
import pt.ipt.dama2026.trekka.data.api.User

/**
 * Atividade responsável pelo registo de novos utilizadores.
 * Envia os dados para o backend e redireciona para o login após sucesso.
 */
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Inicialização de componentes do formulário
        val layoutName = findViewById<TextInputLayout>(R.id.layoutName)
        val editName = findViewById<TextInputEditText>(R.id.editName)
        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmail)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val layoutPassword = findViewById<TextInputLayout>(R.id.layoutPassword)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Limpeza dinâmica de erros para melhor UX
        editName.doOnTextChanged { _, _, _, _ -> layoutName.error = null }
        editEmail.doOnTextChanged { _, _, _, _ -> layoutEmail.error = null }
        editPassword.doOnTextChanged { _, _, _, _ -> layoutPassword.error = null }

        btnBack.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val pass = editPassword.text.toString().trim()

            // Validações locais antes do envio para a rede
            var hasError = false
            if (name.isBlank()) {
                layoutName.error = getString(R.string.error_empty_fields)
                hasError = true
            }
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

            // Processo de registo na API REST
            progressBar.visibility = View.VISIBLE
            btnRegister.isEnabled = false

            lifecycleScope.launch {
                try {
                    RetrofitClient.instance.register(User(name = name, email = email, password = pass))
                    Toast.makeText(this@RegisterActivity, R.string.register_success, Toast.LENGTH_SHORT).show()
                    finish() // Regresso ao Login após criação de conta
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, R.string.register_error, Toast.LENGTH_LONG).show()
                } finally {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                }
            }
        }
    }
}
