package pt.ipt.dama2026.trekka.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room que representa as coordenadas geográficas de um trilho.
 * Cada ponto está associado a um trilho (Trail) através de uma chave estrangeira 
 * com eliminação em cascata (CASCADE), garantindo a integridade dos dados.
 */
@Entity(
    tableName = "trail_points",
    indices = [Index("trailId")],
    foreignKeys = [ForeignKey(
        entity = Trail::class,
        parentColumns = ["id"],
        childColumns = ["trailId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TrailPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trailId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val orderIndex: Int
)
