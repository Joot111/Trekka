package pt.ipt.dama2026.trekka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ipt.dama2026.trekka.data.repository.TrailRepository

/**
 * Fábrica (Factory) personalizada para instanciar o TrailViewModel.
 * Necessária para injetar a dependência do TrailRepository no ViewModel, 
 * seguindo os princípios de Injeção de Dependências.
 */
class TrailViewModelFactory(private val repository: TrailRepository) :
    ViewModelProvider.Factory {
    
    /**
     * Cria uma nova instância do ViewModel solicitado.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrailViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}
