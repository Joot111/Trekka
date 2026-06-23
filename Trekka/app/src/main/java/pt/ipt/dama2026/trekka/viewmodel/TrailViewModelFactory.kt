package pt.ipt.dama2026.trekka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ipt.dama2026.trekka.data.repository.TrailRepository

class TrailViewModelFactory(private val repository: TrailRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}