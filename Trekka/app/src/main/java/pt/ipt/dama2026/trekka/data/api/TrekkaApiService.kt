package pt.ipt.dama2026.trekka.data.api

import retrofit2.http.*

interface TrekkaApiService {

    @POST("auth/register")
    suspend fun register(@Body user: User): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @GET("trails")
    suspend fun getTrails(): List<TrailDTO>

    @POST("trails")
    suspend fun createTrail(@Body trail: TrailDTO): TrailDTO

    @DELETE("trails/{id}")
    suspend fun deleteTrail(@Path("id") id: String)
}
