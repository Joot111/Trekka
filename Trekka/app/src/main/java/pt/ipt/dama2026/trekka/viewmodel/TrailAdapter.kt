package pt.ipt.dama2026.trekka.viewmodel

import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dama2026.trekka.R
import pt.ipt.dama2026.trekka.data.model.Trail
import java.text.SimpleDateFormat
import java.util.*

class TrailAdapter(
    private var trails: List<Trail>,
    private val onItemClick: (Trail) -> Unit,
    private val onEditClick: (Trail) -> Unit,
    private val onDeleteClick: (Trail) -> Unit
) : RecyclerView.Adapter<TrailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.trailName)
        val date: TextView = view.findViewById(R.id.trailDate)
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

        holder.itemView.setOnClickListener { onItemClick(trail) }
        holder.btnEdit.setOnClickListener { onEditClick(trail) }
        holder.btnDelete.setOnClickListener { onDeleteClick(trail) }
    }

    override fun getItemCount() = trails.size

    fun updateData(newTrails: List<Trail>) {
        trails = newTrails
        notifyDataSetChanged()
    }
}