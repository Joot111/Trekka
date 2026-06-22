package pt.ipt.dama2026.trekka.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade que representa a trilha na Base de Dados
 * @see Trail
 */
@Entity(tableName = "trail_points",
    indices = [Index("trailId")],
    foreignKeys = [ForeignKey(
        entity = Trail::class,
        parentColumns = ["id"],
        childColumns = ["trailId"],
        onDelete = ForeignKey.CASCADE
    )]
)

/**
 * Entidade que representa um ponto da trilha na Base de Dados
 * @see TrailPoint
 */
data class TrailPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trailId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val orderIndex: Int
)
