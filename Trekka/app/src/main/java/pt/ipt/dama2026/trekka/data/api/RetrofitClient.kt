package pt.ipt.dama2026.trekka.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // URL base da tua API no Render (substitui pela real quando a tiveres)
    private const val BASE_URL = "https://trekka-api.onrender.com/api/"

    val instance: TrekkaApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(TrekkaApiService::class.java)
    }
}
