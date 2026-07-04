package pt.ipt.dama2026.trekka

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import pt.ipt.dama2026.trekka.data.api.SessionManager
import pt.ipt.dama2026.trekka.service.TrackingService
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModel
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModelFactory

/**
 * Atividade Principal da aplicação Trekka.
 * Gere o início e fim da gravação de trilhos, navegação para histórico/exploração,
 * troca de idioma, autenticação (logout) e alternância de tema claro/escuro.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicialização de componentes da UI
        val startButton = findViewById<Button>(R.id.startButton)
        val historyButton = findViewById<Button>(R.id.historyButton)
        val exploreButton = findViewById<Button>(R.id.exploreButton)
        val languageBox = findViewById<TextView>(R.id.languageBox)
        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        val themeButton = findViewById<ImageButton>(R.id.themeButton)
        val logoutButton = findViewById<ImageButton>(R.id.logoutButton)
        val txtWelcomeUser = findViewById<TextView>(R.id.txtWelcomeUser)

        // Mostrar nome do utilizador autenticado
        val userName = SessionManager(this).fetchUserName()
        if (userName != null) {
            txtWelcomeUser.text = getString(R.string.welcome_user, userName)
        } else {
            txtWelcomeUser.visibility = View.GONE
        }

        // Configuração do ViewModel para gestão de dados do trilho
        val factory = TrailViewModelFactory((application as TrekkaApplication).repository)
        val viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        // Launcher para gerir o pedido de permissão de localização
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                val userId = SessionManager(this).fetchUserId()
                viewModel.startNewTrail("Trilho ${System.currentTimeMillis()}", userId)
                startButton.text = getString(R.string.stop_button)
                startButton.setBackgroundResource(R.drawable.button_stop_ripple)
            }
        }

        // Observa a criação de um novo trilho para iniciar o serviço de tracking em background
        viewModel.currentTrailId.observe(this) { id ->
            if (id != null) {
                val svcIntent = Intent(this, TrackingService::class.java).apply {
                    putExtra("TRAIL_ID", id)
                }
                ContextCompat.startForegroundService(this, svcIntent)
                viewModel.resetCurrentTrailId()
            }
        }

        // Mantém o estado visual do botão se a app for reaberta com o serviço a correr
        if (isServiceRunning(TrackingService::class.java)) {
            startButton.text = getString(R.string.stop_button)
            startButton.setBackgroundResource(R.drawable.button_stop_ripple)
        }

        // Lógica do botão principal (Começar/Parar)
        startButton.setOnClickListener {
            if (isServiceRunning(TrackingService::class.java)) {
                // Ao parar, recuperamos o ID do trilho ativo para definir a privacidade
                val prefs = getSharedPreferences("trekka_prefs", MODE_PRIVATE)
                val activeId = prefs.getLong("active_trail_id", -1)
                
                if (activeId != -1L) {
                    showPrivacyDialog(activeId, startButton)
                } else {
                    finalizeTrail(startButton)
                }
            } else {
                // Antes de começar, verificamos permissões
                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasLocationPermission) {
                    val userId = SessionManager(this).fetchUserId()
                    viewModel.startNewTrail("Trilho ${System.currentTimeMillis()}", userId)
                    startButton.text = getString(R.string.stop_button)
                    startButton.setBackgroundResource(R.drawable.button_stop_ripple)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }

        // Navegação para Histórico Local
        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Navegação para Explorar Trilhos Públicos (Comunitário)
        exploreButton.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        // Ajuste de paddings para o design Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Alternância de Idioma (PT/EN)
        languageBox.setOnClickListener {
            val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "pt"
            val newLocale = if (currentLocale == "pt") "en" else "pt"
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLocale)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        // Acesso à secção "Sobre" (Obrigatório por regulamento)
        aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // Encerramento de Sessão
        logoutButton.setOnClickListener {
            SessionManager(this).logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Alternância de Tema (Modo Escuro / Modo Claro)
        themeButton.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    /**
     * Verifica se um determinado serviço está em execução.
     */
    private fun <T> isServiceRunning(serviceClass: Class<T>): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in am.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) return true
        }
        return false
    }

    /**
     * Exibe o diálogo para o utilizador decidir se o trilho deve ser Público ou Privado.
     */
    private fun showPrivacyDialog(trailId: Long, startButton: Button) {
        val factory = TrailViewModelFactory((application as TrekkaApplication).repository)
        val viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        AlertDialog.Builder(this)
            .setTitle(R.string.privacy_dialog_title)
            .setMessage(R.string.privacy_dialog_msg)
            .setPositiveButton(R.string.public_mode) { _, _ ->
                viewModel.updatePrivacy(trailId, true)
                finalizeTrail(startButton)
            }
            .setNegativeButton(R.string.private_mode) { _, _ ->
                viewModel.updatePrivacy(trailId, false)
                finalizeTrail(startButton)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Encerra o serviço de tracking e limpa o estado visual.
     */
    private fun finalizeTrail(startButton: Button) {
        stopService(Intent(this, TrackingService::class.java))
        startButton.text = getString(R.string.start_button)
        startButton.setBackgroundResource(R.drawable.button_ripple)
        
        getSharedPreferences("trekka_prefs", MODE_PRIVATE).edit { remove("active_trail_id") }
    }
}
