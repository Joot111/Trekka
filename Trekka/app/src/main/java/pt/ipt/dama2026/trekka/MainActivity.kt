package pt.ipt.dama2026.trekka

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import pt.ipt.dama2026.trekka.service.TrackingService
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModel
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModelFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // registar launcher para pedir permissão
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Permissão concedida: iniciar o service
                    val svcIntent = Intent(this, TrackingService::class.java)
                    ContextCompat.startForegroundService(this, svcIntent)
                } else {
                    // Permissão negada: informar o utilizador
                    // Toast / Snackbar / UI feedback
                }
            }

        val factory = TrailViewModelFactory((application as TrekkaApplication).repository)
        val viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        startActivity(Intent(this, HistoryActivity::class.java))

        // botão iniciar
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            // 1. Criar o trilho no DB
            viewModel.startNewTrail("Trilho ${System.currentTimeMillis()}")

            // 2. Observar o ID gerado e iniciar o serviço
            viewModel.currentTrailId.observe(this) { id ->
                if (id != null) {
                    val svcIntent = Intent(this, TrackingService::class.java).apply {
                        putExtra("TRAIL_ID", id)
                    }
                    ContextCompat.startForegroundService(this, svcIntent)
                }
            }
        }

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