package pt.ipt.dama2026.trekka.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dama2026.trekka.R
import pt.ipt.dama2026.trekka.data.api.TrailDTO
import java.text.SimpleDateFormat
import java.util.*

class ExploreAdapter(
    private var trails: List<TrailDTO>,
    private val onItemClick: (TrailDTO) -> Unit
) : RecyclerView.Adapter<ExploreAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.trailName)
        val date: TextView = view.findViewById(R.id.trailDate)
        val metrics: TextView = view.findViewById(R.id.trailMetrics)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditTrail)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteTrail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trail = trails[position]
        holder.name.text = trail.name
        holder.date.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(trail.createdAt))

        // Formatação das métricas
        val distanceKm = trail.distanceMeters / 1000
        val durationFormatted = formatDuration(trail.durationSeconds)
        
        val speedKmH = if (trail.durationSeconds > 0) {
            (distanceKm / (trail.durationSeconds / 3600.0))
        } else {
            0.0
        }

        holder.metrics.text = String.format(
            Locale.getDefault(),
            "%.2f km | %s | %.1f km/h",
            distanceKm,
            durationFormatted,
            speedKmH
        )

        // No Explorar, não permitimos editar ou apagar trilhos dos outros
        holder.btnEdit.visibility = View.GONE
        holder.btnDelete.visibility = View.GONE

        holder.itemView.setOnClickListener { onItemClick(trail) }
    }

    override fun getItemCount() = trails.size

    fun updateData(newTrails: List<TrailDTO>) {
        val diffCallback = TrailDtoDiffCallback(trails, newTrails)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        trails = newTrails
        diffResult.dispatchUpdatesTo(this)
    }

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

    class TrailDtoDiffCallback(
        private val oldList: List<TrailDTO>,
        private val newList: List<TrailDTO>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) = oldList[oldPos].id == newList[newPos].id
        override fun areContentsTheSame(oldPos: Int, newPos: Int) = oldList[oldPos] == newList[newPos]
    }
}
