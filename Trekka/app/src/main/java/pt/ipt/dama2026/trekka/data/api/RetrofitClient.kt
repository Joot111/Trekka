package pt.ipt.dama2026.trekka.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit Singleton.
 * Responsável por configurar e fornecer a instância de comunicação com a API REST 
 * alojada no Render. Utiliza Gson para conversão automática de JSON para objetos Kotlin.
 */
object RetrofitClient {
    
    // URL base da API REST no Render
    private const val BASE_URL = "https://trekka-api.onrender.com/api/"

    // Instância da API inicializada apenas quando necessária
    val instance: TrekkaApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(TrekkaApiService::class.java)
    }
}
