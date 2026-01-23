package com.gymtracker.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.gymtracker.app.data.model.*
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
    
    fun finishWorkout() {
        viewModelScope.launch {
            _activeWorkoutId.value?.let { workoutId ->
                val duration = _workoutStartTime.value?.let {
                    ((System.currentTimeMillis() - it) / 60000).toInt()
                } ?: 0
                repository.completeWorkout(workoutId, duration)
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
    
    fun addExercise(name: String) {
        viewModelScope.launch {
            _activeWorkoutId.value?.let { workoutId ->
                val exerciseId = repository.addExercise(workoutId, name)
                // Ajouter automatiquement une première série
                repository.addSet(exerciseId)
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
    
    // === Export JSON ===
    
    fun exportToJson(context: Context) {
        viewModelScope.launch {
            try {
                val exportData = repository.exportAllData()
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(exportData)
                
                // Créer le fichier
                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
                val fileName = "gym_tracker_export_${dateFormat.format(Date())}.json"
                
                val exportDir = File(context.cacheDir, "exports")
                exportDir.mkdirs()
                val file = File(exportDir, fileName)
                file.writeText(json)
                
                // Partager le fichier
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(shareIntent, "Exporter les données"))
                _uiMessage.emit("Export créé : $fileName")
                
            } catch (e: Exception) {
                _uiMessage.emit("Erreur lors de l'export : ${e.message}")
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
