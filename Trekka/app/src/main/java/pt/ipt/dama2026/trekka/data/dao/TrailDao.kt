package pt.ipt.dama2026.trekka.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ipt.dama2026.trekka.data.model.Trail

/**
 * Interface que define os métodos de acesso aos dados de uma trilha
 * @see Trail
 * @see TrailPoint
 */

@Dao
interface TrailDao {
    @Insert suspend fun insert(trail: Trail): Long
    @Query("SELECT * FROM trails ORDER BY createdAt DESC") fun getAll(): Flow<List<Trail>>
    @Query("SELECT * FROM trails WHERE id = :id") fun getById(id: Long): Flow<Trail?>
    @Delete suspend fun delete(trail: Trail)

    @Update suspend fun update(trail: Trail)

    @Query("DELETE FROM trails WHERE id = :trailId")
    suspend fun deleteById(trailId: Long)

    @Query("UPDATE trails SET name = :newName WHERE id = :trailId")
    suspend fun updateName(trailId: Long, newName: String)

    @Query("UPDATE trails SET distanceMeters = :distance, durationSeconds = :duration WHERE id = :trailId")
    suspend fun updateStats(trailId: Long, distance: Double, duration: Long)
}