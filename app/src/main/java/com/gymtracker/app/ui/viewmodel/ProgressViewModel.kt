package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.SettingsManager
import com.gymtracker.app.data.model.ExerciseProgress
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Statistiques par groupe musculaire
 */
data class MuscleGroupStats(
    val muscleGroup: SettingsManager.Companion.MuscleGroup,
    val totalSets: Int,
    val totalWorkouts: Int,
    val averageSetsPerWorkout: Float
)

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
    
    // Statistiques par groupe musculaire
    val muscleGroupStats: Flow<List<MuscleGroupStats>> = repository.completedWorkoutsWithExercises
        .map { workouts ->
            // Compter les séries par groupe musculaire
            val statsByGroup = mutableMapOf<SettingsManager.Companion.MuscleGroup, MutableList<Int>>()

            for (workout in workouts) {
                // Pour chaque workout, compter les séries par groupe
                val setsPerGroupInWorkout = mutableMapOf<SettingsManager.Companion.MuscleGroup, Int>()

                for (exerciseWithSets in workout.exercises) {
                    val muscleGroup = SettingsManager.getMuscleGroup(exerciseWithSets.exercise.name)
                    val completedSets = exerciseWithSets.sets.count { it.isCompleted }
                    setsPerGroupInWorkout[muscleGroup] =
                        (setsPerGroupInWorkout[muscleGroup] ?: 0) + completedSets
                }

                // Ajouter au total par groupe
                for ((group, sets) in setsPerGroupInWorkout) {
                    if (sets > 0) {
                        statsByGroup.getOrPut(group) { mutableListOf() }.add(sets)
                    }
                }
            }

            // Convertir en liste de MuscleGroupStats
            statsByGroup.map { (group, setsList) ->
                MuscleGroupStats(
                    muscleGroup = group,
                    totalSets = setsList.sum(),
                    totalWorkouts = setsList.size,
                    averageSetsPerWorkout = if (setsList.isNotEmpty()) {
                        setsList.average().toFloat()
                    } else 0f
                )
            }.sortedByDescending { it.totalSets }
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
