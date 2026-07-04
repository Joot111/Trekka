package pt.ipt.dama2026.trekka

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Atividade informativa sobre a aplicação.
 * Exibe dados do desenvolvedor, bibliotecas utilizadas e requisitos académicos (Obrigatório).
 */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)

        val btnBack = findViewById<ImageButton>(R.id.btnBackAbout)
        btnBack.setOnClickListener {
            finish()
        }

        // Ajuste de insets para garantir visibilidade do botão voltar em ecrãs Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnBackAbout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
