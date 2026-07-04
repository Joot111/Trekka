package pt.ipt.dama2026.trekka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.data.api.RetrofitClient
import pt.ipt.dama2026.trekka.viewmodel.ExploreAdapter

/**
 * Atividade para descoberta de trilhos públicos partilhados pela comunidade.
 * Carrega dados em tempo real da API REST e gere a atualização da lista após avaliações.
 */
class ExploreActivity : AppCompatActivity() {

    private lateinit var adapter: ExploreAdapter
    private lateinit var progressBar: ProgressBar

    // Monitoriza o regresso do mapa para atualizar os Ratings na lista se necessário
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadPublicTrails()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explore)

        progressBar = findViewById(R.id.exploreProgressBar)
        val btnBack = findViewById<ImageButton>(R.id.btnBackExplore)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExplore)

        btnBack.setOnClickListener { finish() }

        // Configuração do Adaptador de Exploração (reutiliza layout de itens mas bloqueia edição)
        adapter = ExploreAdapter(emptyList()) { trailDto ->
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("REMOTE_TRAIL", true)
            intent.putExtra("TRAIL_NAME", trailDto.name)
            intent.putExtra("API_TRAIL_ID", trailDto.id)
            mapLauncher.launch(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Carga inicial de dados
        loadPublicTrails()
    }

    /**
     * Faz o pedido à API para obter a lista de todos os trilhos onde isPublic = true.
     */
    private fun loadPublicTrails() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val trails = RetrofitClient.instance.getAllTrails()
                adapter.updateData(trails)
            } catch (e: Exception) {
                Toast.makeText(this@ExploreActivity, R.string.error_loading_explore, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
