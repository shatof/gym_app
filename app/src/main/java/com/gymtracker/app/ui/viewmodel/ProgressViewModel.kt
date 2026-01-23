package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.model.ExerciseProgress
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProgressViewModel(private val repository: GymRepository) : ViewModel() {
    
    // Liste des exercices disponibles
    val exerciseNames: StateFlow<List<String>> = repository.allExerciseNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Exercice sélectionné
    private val _selectedExercise = MutableStateFlow<String?>(null)
    val selectedExercise: StateFlow<String?> = _selectedExercise.asStateFlow()
    
    // Données de progression
    val progressData: Flow<ExerciseProgress?> = _selectedExercise
        .filterNotNull()
        .flatMapLatest { exerciseName ->
            repository.getProgressForExercise(exerciseName)
        }
    
    fun selectExercise(exerciseName: String) {
        _selectedExercise.value = exerciseName
    }
    
    // Factory
    companion object {
        fun provideFactory(repository: GymRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProgressViewModel(repository) as T
                }
            }
        }
    }
}
