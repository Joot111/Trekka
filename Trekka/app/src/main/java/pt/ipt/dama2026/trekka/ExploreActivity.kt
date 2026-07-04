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

class ExploreActivity : AppCompatActivity() {

    private lateinit var adapter: ExploreAdapter
    private lateinit var progressBar: ProgressBar

    // Lançador para detetar se o mapa enviou um voto com sucesso
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Se houve um voto, recarrega a lista para atualizar a média de estrelas
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

        adapter = ExploreAdapter(emptyList()) { trailDto ->
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("REMOTE_TRAIL", true)
            intent.putExtra("TRAIL_NAME", trailDto.name)
            intent.putExtra("API_TRAIL_ID", trailDto.id)
            mapLauncher.launch(intent) // Usar o novo launcher em vez de startActivity
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadPublicTrails()
    }

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
