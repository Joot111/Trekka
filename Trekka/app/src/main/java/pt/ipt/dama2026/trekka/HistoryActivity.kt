package pt.ipt.dama2026.trekka

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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

        adapter = TrailAdapter(
            trails = emptyList(),
            onItemClick = { trail ->
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("TRAIL_ID", trail.id)
                startActivity(intent)
            },
            onEditClick = { trail ->
                showEditDialog(trail.id, trail.name)
            },
            onDeleteClick = { trail ->
                showDeleteConfirmation(trail.id)
            }
        )

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

    private fun showEditDialog(id: Long, currentName: String) {
        val input = EditText(this)
        input.setText(currentName)
        AlertDialog.Builder(this)
            .setTitle(R.string.edit_trail_title)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    viewModel.renameTrail(id, newName)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
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
}