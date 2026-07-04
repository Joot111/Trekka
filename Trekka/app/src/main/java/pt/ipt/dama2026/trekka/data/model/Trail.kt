package pt.ipt.dama2026.trekka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa a trilha na Base de Dados
 * @see Trail
 */

@Entity(tableName = "trails")
data class Trail(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false,
    val userId: String? = null // NOVO: Relacionar trilho local com utilizador logado
)
