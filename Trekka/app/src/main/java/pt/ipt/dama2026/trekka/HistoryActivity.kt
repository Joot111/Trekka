package pt.ipt.dama2026.trekka

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.data.api.PointDTO
import pt.ipt.dama2026.trekka.data.api.RetrofitClient
import pt.ipt.dama2026.trekka.data.api.SessionManager
import pt.ipt.dama2026.trekka.data.api.TrailDTO
import pt.ipt.dama2026.trekka.data.model.Trail
import pt.ipt.dama2026.trekka.viewmodel.TrailAdapter
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModel
import pt.ipt.dama2026.trekka.viewmodel.TrailViewModelFactory

class HistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: TrailViewModel
    private lateinit var adapter: TrailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // 1. Inicializar o ViewModel primeiro
        val repository = (application as TrekkaApplication).repository
        val factory = TrailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        // 2. Configurar o RecyclerView com o Adapter correto
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTrails)

        adapter = TrailAdapter(
            trails = emptyList(),
            onItemClick = { trail ->
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("TRAIL_ID", trail.id)
                startActivity(intent)
            },
            onEditClick = { trail ->
                showEditDialog(trail.id)
            },
            onDeleteClick = { trail ->
                showDeleteConfirmation(trail.id)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 3. Observar a lista de trilhos FILTRADA pelo utilizador logado
        val currentUserId = SessionManager(this).fetchUserId() ?: ""
        viewModel.getTrails(currentUserId).observe(this) { listaDeTrilhos ->
            adapter.updateData(listaDeTrilhos)
        }

        // 4. Lógica de Sincronização (Bidirecional)
        val btnSync = findViewById<ImageButton>(R.id.btnSync)
        btnSync.setOnClickListener {
            syncTrailsWithApi()
        }

        // Ajustar padding para as system bars
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    private fun showEditDialog(id: Long) {
        val repository = (application as TrekkaApplication).repository
        
        lifecycleScope.launch {
            val trail = repository.getTrailsByUser(SessionManager(this@HistoryActivity).fetchUserId() ?: "").first().find { it.id == id } ?: return@launch

            val layout = LinearLayout(this@HistoryActivity)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 10)

            val inputName = EditText(this@HistoryActivity)
            inputName.setText(trail.name)
            inputName.hint = "Nome do trilho"
            layout.addView(inputName)

            val checkPublic = CheckBox(this@HistoryActivity)
            checkPublic.text = getString(R.string.public_mode)
            checkPublic.isChecked = trail.isPublic
            checkPublic.setPadding(0, 20, 0, 20)
            layout.addView(checkPublic)

            AlertDialog.Builder(this@HistoryActivity)
                .setTitle(R.string.edit_trail_title)
                .setView(layout)
                .setPositiveButton(R.string.save) { _, _ ->
                    val newName = inputName.text.toString()
                    if (newName.isNotBlank()) {
                        viewModel.renameTrail(id, newName)
                        viewModel.updatePrivacy(id, checkPublic.isChecked)
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun showDeleteConfirmation(id: Long) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_trail_title)
            .setMessage(R.string.delete_trail_msg)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTrail(id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun syncTrailsWithApi() {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.fetchUserId() ?: return
        val repository = (application as TrekkaApplication).repository

        lifecycleScope.launch {
            try {
                Toast.makeText(this@HistoryActivity, "Sincronizando...", Toast.LENGTH_SHORT).show()

                // --- 1. UPLOAD: Enviar APENAS os trilhos do utilizador logado ---
                val localTrails = repository.getTrailsByUser(userId).first()
                for (trail in localTrails) {
                    val points = repository.getPoints(trail.id).first()
                    val trailDto = TrailDTO(
                        name = trail.name,
                        description = trail.description,
                        distanceMeters = trail.distanceMeters,
                        durationSeconds = trail.durationSeconds,
                        createdAt = trail.createdAt,
                        userId = userId,
                        isPublic = trail.isPublic, // AGORA ENVIA A PRIVACIDADE REAL
                        points = points.map { p ->
                            PointDTO(p.latitude, p.longitude, p.timestamp, p.orderIndex)
                        }
                    )
                    RetrofitClient.instance.createTrail(trailDto)
                }

                // --- 2. DOWNLOAD: Recuperar trilhos da API que não estão locais ---
                val apiTrails = RetrofitClient.instance.getUserTrails(userId)
                
                // Refresh da lista local após o upload para ter a certeza do que já existe
                val updatedLocalTrails = repository.getTrailsByUser(userId).first()

                for (apiTrail in apiTrails) {
                    val exists = updatedLocalTrails.any { it.createdAt == apiTrail.createdAt }
                    if (!exists) {
                        val newTrailId = repository.insertTrail(
                            Trail(
                                name = apiTrail.name,
                                description = apiTrail.description,
                                distanceMeters = apiTrail.distanceMeters,
                                durationSeconds = apiTrail.durationSeconds,
                                createdAt = apiTrail.createdAt,
                                isPublic = apiTrail.isPublic,
                                userId = userId
                            )
                        )
                        for (p in apiTrail.points) {
                            repository.addPoint(newTrailId, p.latitude, p.longitude, p.orderIndex)
                        }
                    }
                }

                Toast.makeText(this@HistoryActivity, "Sincronização concluída!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@HistoryActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
