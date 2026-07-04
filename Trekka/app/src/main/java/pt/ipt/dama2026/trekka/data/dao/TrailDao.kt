package pt.ipt.dama2026.trekka.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ipt.dama2026.trekka.data.model.Trail

/**
 * Interface Data Access Object (DAO) para a entidade Trail.
 * Define as operações de base de dados para persistência, consulta e filtragem por utilizador.
 */
@Dao
interface TrailDao {

    /**
     * Insere um novo trilho na base de dados e retorna o ID gerado.
     */
    @Insert suspend fun insert(trail: Trail): Long

    /**
     * Recupera todos os trilhos pertencentes a um utilizador específico, ordenados pelos mais recentes.
     */
    @Query("SELECT * FROM trails WHERE userId = :userId ORDER BY createdAt DESC") 
    fun getAllByUser(userId: String): Flow<List<Trail>>

    /**
     * Procura um trilho individual pelo seu identificador único.
     */
    @Query("SELECT * FROM trails WHERE id = :id") 
    fun getById(id: Long): Flow<Trail?>

    /**
     * Elimina um objeto trilho da base de dados.
     */
    @Delete suspend fun delete(trail: Trail)

    /**
     * Atualiza os dados de um trilho existente.
     */
    @Update suspend fun update(trail: Trail)

    /**
     * Elimina um trilho pelo seu ID. Os pontos associados são eliminados automaticamente (CASCADE).
     */
    @Query("DELETE FROM trails WHERE id = :trailId")
    suspend fun deleteById(trailId: Long)

    /**
     * Atualiza o nome de um trilho (Renomear).
     */
    @Query("UPDATE trails SET name = :newName WHERE id = :trailId")
    suspend fun updateName(trailId: Long, newName: String)

    /**
     * Guarda os dados finais de desempenho (distância e tempo) após a conclusão do percurso.
     */
    @Query("UPDATE trails SET distanceMeters = :distance, durationSeconds = :duration WHERE id = :trailId")
    suspend fun updateStats(trailId: Long, distance: Double, duration: Long)

    /**
     * Altera o estado de visibilidade (Privado ou Público para a comunidade).
     */
    @Query("UPDATE trails SET isPublic = :isPublic WHERE id = :trailId")
    suspend fun updatePrivacy(trailId: Long, isPublic: Boolean)
}
