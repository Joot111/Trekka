package pt.ipt.dama2026.trekka

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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

/**
 * Atividade que exibe o histórico de trilhos gravados localmente pelo utilizador logado.
 * Permite a edição (nome/privacidade), eliminação, visualização no mapa e sincronização cloud.
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: TrailViewModel
    private lateinit var adapter: TrailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Inicialização do Repositório e ViewModel
        val repository = (application as TrekkaApplication).repository
        val factory = TrailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        // Configuração do RecyclerView com cliques para mapa, edição e eliminação
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTrails)
        adapter = TrailAdapter(
            trails = emptyList(),
            onItemClick = { trail ->
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("TRAIL_ID", trail.id)
                startActivity(intent)
            },
            onEditClick = { trail -> showEditDialog(trail.id) },
            onDeleteClick = { trail -> showDeleteConfirmation(trail.id) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observa os trilhos filtrados pelo ID do utilizador logado
        val currentUserId = SessionManager(this).fetchUserId() ?: ""
        val txtTotalDistance = findViewById<TextView>(R.id.txtTotalDistance)
        val txtTotalTrails = findViewById<TextView>(R.id.txtTotalTrails)

        viewModel.getTrails(currentUserId).observe(this) { listaDeTrilhos ->
            adapter.updateData(listaDeTrilhos)
            
            // Cálculo e exibição das Estatísticas Globais no topo
            val totalDistanceKm = listaDeTrilhos.sumOf { it.distanceMeters } / 1000.0
            txtTotalDistance.text = String.format(java.util.Locale.getDefault(), "%.2f km", totalDistanceKm)
            txtTotalTrails.text = listaDeTrilhos.size.toString()
        }

        // Configuração do botão de Sincronização Cloud (Backup/Restore)
        val btnSync = findViewById<ImageButton>(R.id.btnSync)
        btnSync.setOnClickListener { syncTrailsWithApi() }

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    /**
     * Exibe um diálogo para editar o nome do trilho e alternar o estado de privacidade (Público/Privado).
     */
    private fun showEditDialog(id: Long) {
        val repository = (application as TrekkaApplication).repository
        lifecycleScope.launch {
            val userId = SessionManager(this@HistoryActivity).fetchUserId() ?: ""
            val trail = repository.getTrailsByUser(userId).first().find { it.id == id } ?: return@launch

            val layout = LinearLayout(this@HistoryActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val inputName = EditText(this@HistoryActivity).apply {
                setText(trail.name)
                hint = getString(R.string.edit_trail_title)
            }
            layout.addView(inputName)

            val checkPublic = CheckBox(this@HistoryActivity).apply {
                text = getString(R.string.public_mode)
                isChecked = trail.isPublic
                setPadding(0, 20, 0, 20)
            }
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

    /**
     * Confirmação antes de eliminar um trilho local.
     */
    private fun showDeleteConfirmation(id: Long) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_trail_title)
            .setMessage(R.string.delete_trail_msg)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteTrail(id) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Gere o processo de sincronização bidirecional:
     * 1. Upload: Envia trilhos locais para a API no Render (atualiza se já existir).
     * 2. Download: Recupera trilhos do servidor que não existem no armazenamento local.
     */
    private fun syncTrailsWithApi() {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.fetchUserId() ?: return
        val repository = (application as TrekkaApplication).repository

        lifecycleScope.launch {
            try {
                Toast.makeText(this@HistoryActivity, "Sincronizando...", Toast.LENGTH_SHORT).show()

                // UPLOAD: Sincroniza trilhos locais para a Cloud
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
                        isPublic = trail.isPublic,
                        points = points.map { p -> PointDTO(p.latitude, p.longitude, p.timestamp, p.orderIndex) }
                    )
                    RetrofitClient.instance.createTrail(trailDto)
                }

                // DOWNLOAD: Recupera dados da Cloud para o telemóvel
                val apiTrails = RetrofitClient.instance.getUserTrails(userId)
                val updatedLocalTrails = repository.getTrailsByUser(userId).first()

                for (apiTrail in apiTrails) {
                    val exists = updatedLocalTrails.any { it.createdAt == apiTrail.createdAt }
                    if (!exists) {
                        val newId = repository.insertTrail(
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
                            repository.addPoint(newId, p.latitude, p.longitude, p.orderIndex)
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
