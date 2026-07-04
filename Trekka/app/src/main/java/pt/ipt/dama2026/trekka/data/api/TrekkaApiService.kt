package pt.ipt.dama2026.trekka.data.api

import retrofit2.http.*

/**
 * Classe para envio da avaliação sem usar tipos genéricos (Any).
 */
data class RateRequest(
    val rating: Int,
    val userId: String
)

/**
 * Interface Retrofit que define os endpoints da API REST alojada no Render.
 * Trata da autenticação, gestão de trilhos na cloud e sistema social de avaliações.
 */
interface TrekkaApiService {

    @POST("auth/register")
    suspend fun register(@Body user: User): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    /**
     * Recupera todos os trilhos (públicos e privados) de um utilizador específico (para Backup).
     */
    @GET("trails/user/{userId}")
    suspend fun getUserTrails(@Path("userId") userId: String): List<TrailDTO>

    /**
     * Lista todos os trilhos públicos de todos os utilizadores (para Exploração).
     */
    @GET("trails")
    suspend fun getAllTrails(): List<TrailDTO>

    /**
     * Obtém os detalhes completos de um trilho (incluindo pontos GPS) por ID.
     */
    @GET("trails/{id}")
    suspend fun getTrailById(@Path("id") id: String): TrailDTO

    /**
     * Cria ou atualiza (Upsert) um trilho na nuvem.
     */
    @POST("trails")
    suspend fun createTrail(@Body trail: TrailDTO): TrailDTO

    /**
     * Atualiza dados de um trilho existente (como nome ou privacidade).
     */
    @PUT("trails/{id}")
    suspend fun updateTrail(@Path("id") id: String, @Body trail: TrailDTO): TrailDTO

    /**
     * Envia uma avaliação (estrelas) para um trilho público.
     */
    @POST("trails/{id}/rate")
    suspend fun rateTrail(@Path("id") id: String, @Body body: RateRequest): TrailDTO
}
