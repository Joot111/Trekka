package pt.ipt.dama2026.trekka.data.repository

import pt.ipt.dama2026.trekka.data.database.TrekkaDatabase
import pt.ipt.dama2026.trekka.data.model.Trail
import pt.ipt.dama2026.trekka.data.model.TrailPoint

/**
 * Classe que representa o repositório de dados da trilha
 * @see Trail
 * @see TrailPoint
 */

class TrailRepository(private val db: TrekkaDatabase) {
    val trails = db.trailDao().getAll()

    // Cria uma nova trilha e retorna o seu ID
    suspend fun createTrail(name: String): Long {
        val id = db.trailDao().insert(Trail(name = name))
        return id
    }

    // Adiciona um novo ponto à trilha
    suspend fun addPoint(trailId: Long, lat: Double, lon: Double, idx: Int) {
        db.trailPointDao().insert(
            TrailPoint(
                trailId = trailId,
                latitude = lat,
                longitude = lon,
                timestamp = System.currentTimeMillis(),
                orderIndex = idx
            )
        )
    }

    // Atualiza o trilho com os dados finais
    suspend fun updateTrail(trail: Trail) {
        db.trailDao().update(trail)
    }

    // Elimina um trilho e os seus pontos (o CASCADE trata dos pontos)
    suspend fun deleteTrail(trailId: Long) {
        db.trailDao().deleteById(trailId)
    }

    // Edita apenas o nome
    suspend fun renameTrail(trailId: Long, newName: String) {
        db.trailDao().updateName(trailId, newName)
    }

    // Atualiza as estatísticas finais
    suspend fun updateTrailStats(trailId: Long, distance: Double, duration: Long) {
        db.trailDao().updateStats(trailId, distance, duration)
    }

    // Remove todos os pontos de uma trilha
    fun getPoints(trailId: Long) = db.trailPointDao().getPointsForTrail(trailId)

    // Insere um objeto Trail completo (usado para download da API)
    suspend fun insertTrail(trail: Trail): Long {
        return db.trailDao().insert(trail)
    }
}