package pt.ipt.dama2026.trekka

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import pt.ipt.dama2026.trekka.service.TrackingService
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModel
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModelFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val factory = TrailViewModelFactory((application as TrekkaApplication).repository)
        val viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        // O TEU CÓDIGO AQUI:
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.startNewTrail("Trilho ${System.currentTimeMillis()}")
            }
        }

        // 1. O OBSERVE FICA AQUI (Corre apenas uma vez na criação da Activity)
        viewModel.currentTrailId.observe(this) { id ->
            if (id != null) {
                val svcIntent = Intent(this, TrackingService::class.java).apply {
                    putExtra("TRAIL_ID", id)
                }
                ContextCompat.startForegroundService(this, svcIntent)

                // Limpa o ID para não disparar novamente por engano
                viewModel.resetCurrentTrailId()
            }
        }


        // botão iniciar
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            // Verifica se temos permissão de localização
            val hasLocationPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasLocationPermission) {
                // Se temos permissão, criamos o trilho (o observe iniciará o serviço)
                viewModel.startNewTrail("Trilho ${System.currentTimeMillis()}")
            } else {
                // Se não temos, pedimos a permissão primeiro
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        // botão de histórico
        val historyButton = findViewById<Button>(R.id.historyButton)
        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
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
            val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "pt"
            val newLocale = if (currentLocale == "pt") "en" else "pt"
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLocale)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}