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
import pt.ipt.dama2026.trekka.service.TrackingService

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

        // botão iniciar
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            // cria o intent do service
            val svcIntent = Intent(this, TrackingService::class.java)

            // verifica se já temos permissão
            val hasFineLocation = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation) {
                // Iniciar o serviço (usa startForegroundService para Android O+)
                ContextCompat.startForegroundService(this, svcIntent)
            } else {
                // Pedir permissão; o callback iniciará o serviço se for concedida
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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