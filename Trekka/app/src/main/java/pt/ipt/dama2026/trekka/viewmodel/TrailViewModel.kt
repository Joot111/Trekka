package pt.ipt.dama2026.trekka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.launch
import pt.ipt.dama2026.trekka.data.repository.TrailRepository

/**
 * ViewModel que gere a lógica de negócio dos trilhos para as Atividades.
 * Facilita a comunicação entre a UI e o Repositório, lidando com Coroutines.
 */
class TrailViewModel (private val repo: TrailRepository) : ViewModel() {

    /**
     * Obtém uma lista LiveData de trilhos filtrados por utilizador.
     */
    fun getTrails(userId: String) = repo.getTrailsByUser(userId).asLiveData()

    private val _currentTrailId = MutableLiveData<Long?>()
    val currentTrailId: LiveData<Long?> = _currentTrailId

    /**
     * Inicia a gravação de um novo trilho associado a um utilizador.
     */
    fun startNewTrail(name: String, userId: String?) = viewModelScope.launch {
        val id = repo.createTrail(name, userId)
        _currentTrailId.value = id
    }

    /**
     * Adiciona um ponto de localização à sessão de tracking ativa.
     */
    fun addPoint(lat: Double, lon: Double, idx: Int) = viewModelScope.launch {
        _currentTrailId.value?.let { repo.addPoint(it, lat, lon, idx) }
    }

    /**
     * Reseta o ID do trilho atual para evitar reinicializações indesejadas do serviço.
     */
    fun resetCurrentTrailId() {
        _currentTrailId.value = null
    }

    /**
     * Elimina um trilho e os seus pontos do armazenamento local.
     */
    fun deleteTrail(id: Long) = viewModelScope.launch {
        repo.deleteTrail(id)
    }

    /**
     * Renomeia um trilho existente localmente.
     */
    fun renameTrail(id: Long, newName: String) = viewModelScope.launch {
        repo.renameTrail(id, newName)
    }

    /**
     * Altera as definições de privacidade (Público/Privado) de um trilho.
     */
    fun updatePrivacy(id: Long, isPublic: Boolean) = viewModelScope.launch {
        repo.updateTrailPrivacy(id, isPublic)
    }
}
