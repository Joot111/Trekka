package pt.ipt.dama2026.trekka.viewmodel

import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dama2026.trekka.R
import pt.ipt.dama2026.trekka.data.model.Trail
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adaptador para a listagem de trilhos no Histórico Local.
 * Gere a visualização de métricas (distância, tempo, velocidade) e a lógica de dificuldade inteligente.
 */
class TrailAdapter(
    private var trails: List<Trail>,
    private val onItemClick: (Trail) -> Unit,
    private val onEditClick: (Trail) -> Unit,
    private val onDeleteClick: (Trail) -> Unit
) : RecyclerView.Adapter<TrailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.trailName)
        val date: TextView = view.findViewById(R.id.trailDate)
        val metrics: TextView = view.findViewById(R.id.trailMetrics)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditTrail)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteTrail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_trail, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trail = trails[position]
        holder.name.text = trail.name
        holder.date.text =
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(trail.createdAt))

        // Formatação das métricas
        val distanceKm = trail.distanceMeters / 1000
        val durationFormatted = formatDuration(trail.durationSeconds)
        
        // Cálculo da Velocidade Média (km/h)
        val speedKmH = if (trail.durationSeconds > 0) {
            (distanceKm / (trail.durationSeconds / 3600.0))
        } else {
            0.0
        }

        // Lógica de "IA" para classificação de dificuldade baseada na distância
        val difficulty = when {
            distanceKm < 1.0 -> holder.itemView.context.getString(R.string.difficulty_easy)
            distanceKm < 3.0 -> holder.itemView.context.getString(R.string.difficulty_moderate)
            else -> holder.itemView.context.getString(R.string.difficulty_hard)
        }

        holder.metrics.text = String.format(
            Locale.getDefault(),
            "%.2f km | %s | %.1f km/h | %s",
            distanceKm,
            durationFormatted,
            speedKmH,
            difficulty
        )

        holder.itemView.setOnClickListener { onItemClick(trail) }
        holder.btnEdit.setOnClickListener { onEditClick(trail) }
        holder.btnDelete.setOnClickListener { onDeleteClick(trail) }
    }

    override fun getItemCount() = trails.size

    /**
     * Atualiza a lista de trilhos de forma eficiente usando DiffUtil.
     */
    fun updateData(newTrails: List<Trail>) {
        val diffCallback = TrailDiffCallback(trails, newTrails)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        trails = newTrails
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Converte segundos num formato legível HH:mm:ss ou mm:ss.
     */
    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", m, s)
        }
    }

    class TrailDiffCallback(
        private val oldList: List<Trail>,
        private val newList: List<Trail>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
