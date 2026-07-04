package pt.ipt.dama2026.trekka.data.api

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object (DTO) para representar um trilho na comunicação com a API REST.
 * Contém metadados do percurso, definições de privacidade e dados sociais (ratings).
 */
data class TrailDTO(
    @SerializedName("_id")
    val id: String? = null,        // ID gerado pelo MongoDB Atlas
    val name: String,              // Nome atribuído ao trilho
    val description: String?,      // Descrição opcional
    val distanceMeters: Double,    // Distância total percorrida
    val durationSeconds: Long,     // Tempo total de gravação
    val createdAt: Long,           // Timestamp de criação (usado como chave de sincronização)
    val userId: String?,           // ID do autor do trilho na cloud
    val isPublic: Boolean = true,  // Estado de visibilidade comunitária
    val rating: Float = 0f,        // Média de avaliações recebidas
    val numRatings: Int = 0,       // Quantidade total de votos
    val ratedBy: List<String> = emptyList(), // Lista de IDs de utilizadores que já avaliaram
    val points: List<PointDTO>     // Lista detalhada de coordenadas GPS
)

/**
 * Representa um ponto geográfico individual capturado durante o percurso.
 */
data class PointDTO(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val orderIndex: Int // Garante a ordem correta do traçado no mapa
)
