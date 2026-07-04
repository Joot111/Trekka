package pt.ipt.dama2026.trekka.data.repository

import pt.ipt.dama2026.trekka.data.database.TrekkaDatabase
import pt.ipt.dama2026.trekka.data.model.Trail
import pt.ipt.dama2026.trekka.data.model.TrailPoint

/**
 * Repositório central para gestão de dados de trilhos e pontos GPS.
 * Faz a mediação entre a base de dados Room (Local) e o ViewModel.
 */
class TrailRepository(private val db: TrekkaDatabase) {

    /**
     * Obtém todos os trilhos filtrados por ID de utilizador.
     */
    fun getTrailsByUser(userId: String) = db.trailDao().getAllByUser(userId)

    /**
     * Cria um novo trilho associado a um utilizador e retorna o seu ID autogerado.
     */
    suspend fun createTrail(name: String, userId: String?): Long {
        val id = db.trailDao().insert(Trail(name = name, userId = userId))
        return id
    }

    /**
     * Adiciona um ponto GPS a um trilho específico.
     */
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

    /**
     * Atualiza os dados de um trilho completo (usado no download da API).
     */
    suspend fun updateTrail(trail: Trail) {
        db.trailDao().update(trail)
    }

    /**
     * Elimina um trilho e os seus pontos associados (via CASCADE).
     */
    suspend fun deleteTrail(trailId: Long) {
        db.trailDao().deleteById(trailId)
    }

    /**
     * Altera o nome de um trilho existente.
     */
    suspend fun renameTrail(trailId: Long, newName: String) {
        db.trailDao().updateName(trailId, newName)
    }

    /**
     * Atualiza as estatísticas de distância e duração ao finalizar a gravação.
     */
    suspend fun updateTrailStats(trailId: Long, distance: Double, duration: Long) {
        db.trailDao().updateStats(trailId, distance, duration)
    }

    /**
     * Altera o estado de privacidade (Público/Privado).
     */
    suspend fun updateTrailPrivacy(trailId: Long, isPublic: Boolean) {
        db.trailDao().updatePrivacy(trailId, isPublic)
    }

    /**
     * Obtém todos os pontos GPS de um trilho específico para desenho no mapa.
     */
    fun getPoints(trailId: Long) = db.trailPointDao().getPointsForTrail(trailId)

    /**
     * Insere um objeto Trail completo (usado para reconstruir o histórico após download).
     */
    suspend fun insertTrail(trail: Trail): Long {
        return db.trailDao().insert(trail)
    }
}
