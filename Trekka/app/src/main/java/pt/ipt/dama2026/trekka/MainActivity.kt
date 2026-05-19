package pt.ipt.dama2026.trekka

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Ajustar padding para as barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Lógica de troca de idioma
        val languageBox = findViewById<TextView>(R.id.languageBox)
        languageBox.setOnClickListener {
            // Obter o idioma atual (ou "pt" como padrão)
            val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "pt"
            
            // Alternar entre PT e EN
            val newLocale = if (currentLocale == "pt") "en" else "pt"
            
            // Aplicar o novo idioma
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLocale)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}