package pt.ipt.dama2026.trekka

import android.Manifest
import android.app.ActivityManager
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
import pt.ipt.dama2026.trekka.service.TrackingService
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModel
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModelFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. PRIMEIRO: Encontrar os botões no layout
        val startButton = findViewById<Button>(R.id.startButton)
        val historyButton = findViewById<Button>(R.id.historyButton)
        val languageBox = findViewById<TextView>(R.id.languageBox)
        val aboutButton = findViewById<android.widget.ImageButton>(R.id.aboutButton)

        // 2. SEGUNDO: Inicializar o ViewModel
        val factory = TrailViewModelFactory((application as TrekkaApplication).repository)
        val viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        // 3. TERCEIRO: Configurar o Launcher de Permissões (agora ele já conhece o startButton)
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.startNewTrail("Trilho ${System.currentTimeMillis()}")
                startButton.text = getString(R.string.stop_button)
                startButton.setBackgroundResource(R.drawable.button_stop_ripple)
            }
        }

        // 4. Configurar o Observer para iniciar o serviço quando o ID for criado
        viewModel.currentTrailId.observe(this) { id ->
            if (id != null) {
                val svcIntent = Intent(this, TrackingService::class.java).apply {
                    putExtra("TRAIL_ID", id)
                }
                ContextCompat.startForegroundService(this, svcIntent)
                viewModel.resetCurrentTrailId()
            }
        }

        // 5. Definir o texto inicial do botão se o serviço já estiver a correr
        if (isServiceRunning(TrackingService::class.java)) {
            startButton.text = getString(R.string.stop_button)
            startButton.setBackgroundResource(R.drawable.button_stop_ripple)
        }

        // Clique do botão Iniciar/Parar
        startButton.setOnClickListener {
            if (isServiceRunning(TrackingService::class.java)) {
                stopService(Intent(this, TrackingService::class.java))
                startButton.text = getString(R.string.start_button)
                startButton.setBackgroundResource(R.drawable.button_ripple)
            } else {
                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasLocationPermission) {
                    viewModel.startNewTrail("Trilho ${System.currentTimeMillis()}")
                    startButton.text = getString(R.string.stop_button)
                    startButton.setBackgroundResource(R.drawable.button_stop_ripple)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }

        // Clique do botão Histórico
        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Ajustar padding das barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Clique para trocar idioma
        languageBox.setOnClickListener {
            val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "pt"
            val newLocale = if (currentLocale == "pt") "en" else "pt"
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLocale)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        // Clique para abrir o Sobre
        aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    // Função auxiliar para verificar o estado do serviço
    private fun <T> isServiceRunning(serviceClass: Class<T>): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in am.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) return true
        }
        return false
    }
}