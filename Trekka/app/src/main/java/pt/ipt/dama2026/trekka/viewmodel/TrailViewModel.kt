package pt.ipt.dama2026.trekka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.data.repository.TrailRepository

/**
 * Classe que representa o ViewModel da tela principal
 */

class TrailViewModel (private val repo: TrailRepository) : ViewModel() {

    // Lista de trilhas
    val trails = repo.trails.asLiveData()

    private val _currentTrailId = MutableLiveData<Long?>()
    val currentTrailId: LiveData<Long?> = _currentTrailId

    // Inicia uma nova trilha
    fun startNewTrail(name: String) = viewModelScope.launch {
        val id = repo.createTrail(name)
        _currentTrailId.value = id
    }

    // Adiciona um novo ponto à trilha
    fun addPoint(lat: Double, lon: Double, idx: Int) = viewModelScope.launch {
        _currentTrailId.value?.let { repo.addPoint(it, lat, lon, idx) }
    }

    // Limpa o ID da trilha atual para evitar re-emissão de eventos (como ao rodar o ecrã)
    fun resetCurrentTrailId() {
        _currentTrailId.value = null
    }

    // Elimina um trilho
    fun deleteTrail(id: Long) = viewModelScope.launch {
        repo.deleteTrail(id)
    }

    // Renomeia um trilho
    fun renameTrail(id: Long, newName: String) = viewModelScope.launch {
        repo.renameTrail(id, newName)
    }
}