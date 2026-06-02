package pt.ipt.dama2026.trekka.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pt.ipt.dama2026.trekka.data.model.TrailPoint

/**
 * Interface que define os métodos de acesso aos dados de um ponto da trilha
 * @see TrailPoint
 */

@Dao
interface TrailPointDao {
    @Insert suspend fun insert(point: TrailPoint): Long
    @Query("SELECT * FROM trail_points WHERE trailId = :trailId ORDER BY orderIndex ASC")
        fun getPointsForTrail(trailId: Long): Flow<List<TrailPoint>>
    @Query("DELETE FROM trail_points WHERE trailId = :trailId")
        suspend fun deleteForTrail(trailId: Long)
}