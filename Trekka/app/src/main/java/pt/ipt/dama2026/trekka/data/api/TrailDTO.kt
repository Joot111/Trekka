package pt.ipt.dama2026.trekka.data.api

import com.google.gson.annotations.SerializedName

data class TrailDTO(
    @SerializedName("_id")
    val id: String? = null,
    val name: String,
    val description: String?,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val createdAt: Long,
    val userId: String?,
    val points: List<PointDTO>
)

data class PointDTO(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val orderIndex: Int
)
