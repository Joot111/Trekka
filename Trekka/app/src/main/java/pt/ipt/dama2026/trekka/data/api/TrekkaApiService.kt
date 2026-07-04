package pt.ipt.dama2026.trekka.data.api

import retrofit2.http.*

// NOVO: Classe para enviar a avaliação sem usar tipos genéricos (Any)
data class RateRequest(
    val rating: Int,
    val userId: String
)

interface TrekkaApiService {

    @POST("auth/register")
    suspend fun register(@Body user: User): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @GET("trails/user/{userId}")
    suspend fun getUserTrails(@Path("userId") userId: String): List<TrailDTO>

    @GET("trails")
    suspend fun getAllTrails(): List<TrailDTO>

    @GET("trails/{id}")
    suspend fun getTrailById(@Path("id") id: String): TrailDTO

    @POST("trails")
    suspend fun createTrail(@Body trail: TrailDTO): TrailDTO

    @PUT("trails/{id}")
    suspend fun updateTrail(@Path("id") id: String, @Body trail: TrailDTO): TrailDTO

    @POST("trails/{id}/rate")
    suspend fun rateTrail(@Path("id") id: String, @Body body: RateRequest): TrailDTO
}
