package pt.ipt.dama2026.trekka.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pt.ipt.dama2026.trekka.data.model.TrailPoint

/**
 * Interface Data Access Object (DAO) para os pontos geográficos.
 * Define as operações de base de dados para inserção, consulta e remoção de coordenadas GPS.
 */
@Dao
interface TrailPointDao {
    
    /**
     * Insere uma nova coordenada associada a um trilho.
     */
    @Insert suspend fun insert(point: TrailPoint): Long

    /**
     * Recupera todos os pontos de um trilho específico ordenados pela ordem de recolha.
     */
    @Query("SELECT * FROM trail_points WHERE trailId = :trailId ORDER BY orderIndex ASC")
    fun getPointsForTrail(trailId: Long): Flow<List<TrailPoint>>

    /**
     * Remove todos os pontos pertencentes a um trilho (utilizado em limpezas manuais).
     */
    @Query("DELETE FROM trail_points WHERE trailId = :trailId")
    suspend fun deleteForTrail(trailId: Long)
}
