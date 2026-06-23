package pt.ipt.dama2026.trekka

import android.os.Bundle
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

        // Configurar o RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTrails)
        adapter = TrailAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar ViewModel (usando a mesma lógica da MainActivity)
        val repository = (application as TrekkaApplication).repository
        val factory = TrailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        // Observar a lista de trilhos e atualizar o adapter automaticamente
        viewModel.trails.observe(this) { listaDeTrilhos ->
            adapter.updateData(listaDeTrilhos)
        }

        // Ajustar padding para as system bars (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerViewTrails)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }
}
