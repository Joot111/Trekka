package pt.ipt.dama2026.trekka

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        
        adapter = TrailAdapter(emptyList()) { trail ->
            // Quando clicamos num trilho, vamos para o mapa
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("TRAIL_ID", trail.id)
            startActivity(intent)
        }
        
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 3. Observar a lista de trilhos
        viewModel.trails.observe(this) { listaDeTrilhos ->
            adapter.updateData(listaDeTrilhos)
        }

        // Ajustar padding para as system bars
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }
}
