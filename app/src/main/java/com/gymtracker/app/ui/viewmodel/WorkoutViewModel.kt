package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.model.*
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(private val repository: GymRepository) : ViewModel() {
    
    // État de la séance active
    private val _activeWorkoutId = MutableStateFlow<Long?>(null)
    val activeWorkoutId: StateFlow<Long?> = _activeWorkoutId.asStateFlow()
    
    private val _workoutStartTime = MutableStateFlow<Long?>(null)
    val workoutStartTime: StateFlow<Long?> = _workoutStartTime.asStateFlow()
    
    // Exercices de la séance active
    val activeWorkoutExercises: Flow<List<ExerciseWithSets>> = _activeWorkoutId
        .filterNotNull()
        .flatMapLatest { workoutId ->
            repository.getExercisesWithSetsForWorkout(workoutId)
        }
    
    // Historique des séances
    val allWorkouts: Flow<List<WorkoutWithExercises>> = repository.allWorkoutsWithExercises
    
    // Noms d'exercices pour l'autocomplétion
    val exerciseNames: Flow<List<String>> = repository.allExerciseNames
    
    // Templates disponibles
    val allTemplates: Flow<List<TemplateWithExercises>> = repository.allTemplatesWithExercises

    // Messages UI
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()
    
    init {
        // Vérifier s'il y a une séance en cours au démarrage
        viewModelScope.launch {
            repository.getActiveWorkout()?.let { workout ->
                _activeWorkoutId.value = workout.id
                _workoutStartTime.value = workout.date
            }
        }
    }
    
    // === Gestion des séances ===
    
    fun startWorkout() {
        viewModelScope.launch {
            val workoutId = repository.createWorkout()
            _activeWorkoutId.value = workoutId
            _workoutStartTime.value = System.currentTimeMillis()
        }
    }
    
    fun startWorkoutFromTemplate(templateId: Long) {
        viewModelScope.launch {
            try {
                val workoutId = repository.createWorkoutFromTemplate(templateId)
                _activeWorkoutId.value = workoutId
                _workoutStartTime.value = System.currentTimeMillis()
                _uiMessage.emit("Séance démarrée")
            } catch (e: Exception) {
                _uiMessage.emit("Erreur: ${e.message}")
            }
        }
    }

    fun finishWorkout(notes: String = "") {
        viewModelScope.launch {
            _activeWorkoutId.value?.let { workoutId ->
                val duration = _workoutStartTime.value?.let {
                    ((System.currentTimeMillis() - it) / 60000).toInt()
                } ?: 0
                repository.completeWorkout(workoutId, duration, notes)
                _activeWorkoutId.value = null
                _workoutStartTime.value = null
                _uiMessage.emit("Séance terminée ! Durée: $duration minutes")
            }
        }
    }
    
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
            _uiMessage.emit("Séance supprimée")
        }
    }
    
    // === Gestion des exercices ===
    
    fun addExercise(name: String, restTimeSeconds: Int = 90) {
        viewModelScope.launch {
            _activeWorkoutId.value?.let { workoutId ->
                val exerciseId = repository.addExercise(workoutId, name, restTimeSeconds)

                // Récupérer les dernières valeurs pour cet exercice
                val lastSet = repository.getLastSetValuesForExercise(name)
                val defaultWeight = lastSet?.weight ?: 0f
                val defaultReps = lastSet?.reps ?: 0
                val defaultMiorep = lastSet?.miorep

                // Ajouter automatiquement une première série avec les dernières valeurs
                repository.addSet(exerciseId, defaultReps, defaultWeight, defaultMiorep)
            }
        }
    }
    
    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }
    
    // === Gestion des séries ===
    
    fun addSet(exerciseId: Long, copyLastSet: Boolean = true) {
        viewModelScope.launch {
            val sets = repository.getSetsForExercise(exerciseId).first()
            val lastSet = sets.lastOrNull()
            
            if (copyLastSet && lastSet != null) {
                repository.addSet(
                    exerciseId = exerciseId,
                    reps = lastSet.reps,
                    weight = lastSet.weight,
                    miorep = lastSet.miorep
                )
            } else {
                repository.addSet(exerciseId)
            }
        }
    }
    
    fun updateSet(set: ExerciseSet) {
        viewModelScope.launch {
            repository.updateSet(set)
        }
    }
    
    fun updateSetValues(setId: Long, reps: Int, weight: Float, miorep: Int?) {
        viewModelScope.launch {
            repository.updateSetValues(setId, reps, weight, miorep)
        }
    }
    
    fun toggleSetCompletion(setId: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleSetCompletion(setId, completed)
        }
    }
    
    fun deleteSet(set: ExerciseSet) {
        viewModelScope.launch {
            repository.deleteSet(set)
        }
    }
    
    // Quick increment/decrement
    fun incrementReps(set: ExerciseSet) {
        viewModelScope.launch {
            repository.updateSetValues(set.id, set.reps + 1, set.weight, set.miorep)
        }
    }
    
    fun decrementReps(set: ExerciseSet) {
        viewModelScope.launch {
            if (set.reps > 0) {
                repository.updateSetValues(set.id, set.reps - 1, set.weight, set.miorep)
            }
        }
    }
    
    fun incrementWeight(set: ExerciseSet, increment: Float = 2.5f) {
        viewModelScope.launch {
            repository.updateSetValues(set.id, set.reps, set.weight + increment, set.miorep)
        }
    }
    
    fun decrementWeight(set: ExerciseSet, decrement: Float = 2.5f) {
        viewModelScope.launch {
            if (set.weight >= decrement) {
                repository.updateSetValues(set.id, set.reps, set.weight - decrement, set.miorep)
            }
        }
    }
    

    // Factory
    companion object {
        fun provideFactory(repository: GymRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WorkoutViewModel(repository) as T
                }
            }
        }
    }
}
