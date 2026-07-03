package pt.ipt.dama2026.trekka

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.data.api.RetrofitClient
import pt.ipt.dama2026.trekka.data.api.User

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val editName = findViewById<TextInputEditText>(R.id.editName)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnBack.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val name = editName.text.toString()
            val email = editEmail.text.toString()
            val pass = editPassword.text.toString()

            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnRegister.isEnabled = false

            lifecycleScope.launch {
                try {
                    RetrofitClient.instance.register(User(name = name, email = email, password = pass))
                    Toast.makeText(this@RegisterActivity, R.string.register_success, Toast.LENGTH_SHORT).show()
                    finish() // Volta para o Login
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
