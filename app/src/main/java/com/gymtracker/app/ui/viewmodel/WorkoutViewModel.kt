package com.gymtracker.app.ui.viewmodel

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.model.*
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.util.autoExportData
import com.gymtracker.app.widget.GymWidgetProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(private val repository: GymRepository) : ViewModel() {

    // Debounce pour les mises à jour de sets
    private var updateSetJob: Job? = null
    private val pendingUpdates = mutableMapOf<Long, ExerciseSet>()

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

    // ────────────────────────────────────────────────
    // Groupe B — Undo intelligent
    // ────────────────────────────────────────────────

    sealed class UndoBuffer {
        data class DeletedSet(val set: ExerciseSet) : UndoBuffer()
        data class DeletedExercise(val exercise: Exercise, val sets: List<ExerciseSet>) : UndoBuffer()
    }
    private var undoBuffer: UndoBuffer? = null

    private val _undoEvent = MutableSharedFlow<String>()
    val undoEvent: SharedFlow<String> = _undoEvent.asSharedFlow()

    fun undoLast() {
        val buffer = undoBuffer ?: return
        undoBuffer = null
        viewModelScope.launch {
            when (buffer) {
                is UndoBuffer.DeletedSet -> {
                    repository.insertSetDirectly(buffer.set)
                    _uiMessage.emit("Série restaurée")
                }
                is UndoBuffer.DeletedExercise -> {
                    repository.insertExerciseDirectly(buffer.exercise)
                    buffer.sets.forEach { repository.insertSetDirectly(it) }
                    _uiMessage.emit("\"${buffer.exercise.name}\" restauré")
                }
            }
        }
    }

    // ────────────────────────────────────────────────
    // Groupe A — PR events
    // ────────────────────────────────────────────────

    private val _prEvent = MutableSharedFlow<PrEvent>()
    val prEvent: SharedFlow<PrEvent> = _prEvent.asSharedFlow()

    /** Exercices dont le PR a déjà été annoncé dans la séance courante */
    private val announcedPRs = mutableSetOf<String>()

    // ────────────────────────────────────────────────
    // Groupe A — suggestions de charge (par exerciceId)
    // ────────────────────────────────────────────────

    private val _loadSuggestions = MutableStateFlow<Map<Long, LoadSuggestion>>(emptyMap())
    val loadSuggestions: StateFlow<Map<Long, LoadSuggestion>> = _loadSuggestions.asStateFlow()

    // ────────────────────────────────────────────────
    // Groupe A — stats hebdomadaires
    // ────────────────────────────────────────────────

    private val _weeklyStats = MutableStateFlow<WeeklyStats?>(null)
    val weeklyStats: StateFlow<WeeklyStats?> = _weeklyStats.asStateFlow()

    init {
        // Vérifier s'il y a une séance en cours au démarrage
        viewModelScope.launch {
            repository.getActiveWorkout()?.let { workout ->
                _activeWorkoutId.value = workout.id
                _workoutStartTime.value = workout.date
            }
            _weeklyStats.value = repository.getWeeklyStats()
        }

        // Actualiser les suggestions de charge à chaque changement d'exercices
        viewModelScope.launch {
            activeWorkoutExercises.collectLatest { exercises ->
                if (exercises.isNotEmpty()) {
                    val suggestions = mutableMapOf<Long, LoadSuggestion>()
                    exercises.forEach { ews ->
                        repository.getLoadSuggestion(ews.exercise.name)?.let {
                            suggestions[ews.exercise.id] = it
                        }
                    }
                    _loadSuggestions.value = suggestions
                }
            }
        }
    }

    // === Gestion des séances ===

    fun startWorkout() {
        viewModelScope.launch {
            val workoutId = repository.createWorkout()
            _activeWorkoutId.value = workoutId
            _workoutStartTime.value = System.currentTimeMillis()
            announcedPRs.clear()
        }
    }

    fun startWorkoutFromTemplate(templateId: Long) {
        viewModelScope.launch {
            try {
                val workoutId = repository.createWorkoutFromTemplate(templateId)
                _activeWorkoutId.value = workoutId
                _workoutStartTime.value = System.currentTimeMillis()
                announcedPRs.clear()
                _uiMessage.emit("Séance démarrée")
            } catch (e: Exception) {
                _uiMessage.emit("Erreur: ${e.message}")
            }
        }
    }

    fun finishWorkout(notes: String = "", context: Context? = null) {
        viewModelScope.launch {
            _activeWorkoutId.value?.let { workoutId ->
                val duration = _workoutStartTime.value?.let {
                    ((System.currentTimeMillis() - it) / 60000).toInt()
                } ?: 0
                repository.completeWorkout(workoutId, duration, notes)
                _activeWorkoutId.value = null
                _workoutStartTime.value = null
                announcedPRs.clear()
                _loadSuggestions.value = emptyMap()
                _uiMessage.emit("Séance terminée ! Durée: $duration minutes")
                _weeklyStats.value = repository.getWeeklyStats()

                // Export automatique silencieux
                context?.let {
                    autoExportData(
                        context = it,
                        repository = repository,
                        onSuccess = { },
                        onError = { }
                    )
                    // Rafraîchir le widget
                    val manager = AppWidgetManager.getInstance(it)
                    val ids = manager.getAppWidgetIds(ComponentName(it, GymWidgetProvider::class.java))
                    if (ids.isNotEmpty()) {
                        GymWidgetProvider().onUpdate(it, manager, ids)
                    }
                }
            }
        }
    }

    fun cancelWorkout() {
        viewModelScope.launch {
            _activeWorkoutId.value?.let { workoutId ->
                repository.deleteWorkoutById(workoutId)
                _activeWorkoutId.value = null
                _workoutStartTime.value = null
                announcedPRs.clear()
                _loadSuggestions.value = emptyMap()
                _uiMessage.emit("Séance annulée")
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
            _uiMessage.emit("Séance supprimée")
        }
    }

    fun updateWorkoutHistory(workout: Workout, updatedSets: List<ExerciseSet>) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
            updatedSets.forEach { set -> repository.updateSet(set) }
            _uiMessage.emit("Séance modifiée")
        }
    }

    // === Gestion des exercices ===

    fun addExercise(name: String, restTimeSeconds: Int = 90) {
        viewModelScope.launch {
            _activeWorkoutId.value?.let { workoutId ->
                val exerciseId = repository.addExercise(workoutId, name, restTimeSeconds)
                val lastSet = repository.getLastSetValuesForExercise(name)
                repository.addSet(exerciseId, lastSet?.reps ?: 0, lastSet?.weight ?: 0f, lastSet?.miorep)
            }
        }
    }

    fun deleteExercise(exerciseWithSets: ExerciseWithSets) {
        undoBuffer = UndoBuffer.DeletedExercise(exerciseWithSets.exercise, exerciseWithSets.sets)
        viewModelScope.launch {
            repository.deleteExercise(exerciseWithSets.exercise)
            _undoEvent.emit("\"${exerciseWithSets.exercise.name}\" supprimé")
        }
    }

    // === Gestion des séries ===

    fun addSet(exerciseId: Long, copyLastSet: Boolean = true) {
        viewModelScope.launch {
            val sets = repository.getSetsForExercise(exerciseId).first()
            val lastSet = sets.lastOrNull()
            if (copyLastSet && lastSet != null) {
                repository.addSet(exerciseId = exerciseId, reps = lastSet.reps, weight = lastSet.weight, miorep = lastSet.miorep)
            } else {
                repository.addSet(exerciseId)
            }
        }
    }

    fun updateSet(set: ExerciseSet) {
        pendingUpdates[set.id] = set
        updateSetJob?.cancel()
        updateSetJob = viewModelScope.launch {
            delay(300)
            pendingUpdates[set.id]?.let { pendingSet ->
                repository.updateSet(pendingSet)
                pendingUpdates.remove(set.id)
            }
        }
    }

    fun updateSetValues(setId: Long, reps: Int, weight: Float, miorep: Int?) {
        viewModelScope.launch { repository.updateSetValues(setId, reps, weight, miorep) }
    }

    fun toggleSetCompletion(setId: Long, completed: Boolean) {
        viewModelScope.launch { repository.toggleSetCompletion(setId, completed) }
    }

    fun deleteSet(set: ExerciseSet) {
        undoBuffer = UndoBuffer.DeletedSet(set)
        viewModelScope.launch {
            repository.deleteSet(set)
            _undoEvent.emit("Série ${set.setNumber} supprimée")
        }
    }

    // Quick increment/decrement
    fun incrementReps(set: ExerciseSet) {
        viewModelScope.launch { repository.updateSetValues(set.id, set.reps + 1, set.weight, set.miorep) }
    }

    fun decrementReps(set: ExerciseSet) {
        viewModelScope.launch { if (set.reps > 0) repository.updateSetValues(set.id, set.reps - 1, set.weight, set.miorep) }
    }

    fun incrementWeight(set: ExerciseSet, increment: Float = 1.25f) {
        viewModelScope.launch { repository.updateSetValues(set.id, set.reps, set.weight + increment, set.miorep) }
    }

    fun decrementWeight(set: ExerciseSet, decrement: Float = 1.25f) {
        viewModelScope.launch { if (set.weight >= decrement) repository.updateSetValues(set.id, set.reps, set.weight - decrement, set.miorep) }
    }

    // ────────────────────────────────────────────────
    // Groupe A — détection de PR
    // ────────────────────────────────────────────────

    /**
     * Vérifie si [weight]/[reps]/[miorep] constitue un nouveau record pour [exerciseName]
     * et émet un [PrEvent] si oui. Chaque exercice n'annonce qu'un seul PR par séance.
     */
    fun checkAndEmitPR(exerciseName: String, weight: Float, reps: Int, miorep: Int?) {
        if (exerciseName in announcedPRs || weight <= 0f) return
        viewModelScope.launch {
            val historicalMaxWeight = repository.getMaxWeightForExercise(exerciseName)
            val historicalMax1RM   = repository.getBest1RMForExercise(exerciseName)
            val current1RM = calculate1RM(weight, reps, miorep)
            when {
                historicalMaxWeight > 0f && weight > historicalMaxWeight -> {
                    announcedPRs.add(exerciseName)
                    _prEvent.emit(PrEvent(exerciseName, PrType.WEIGHT, weight, historicalMaxWeight))
                }
                historicalMax1RM > 0f && current1RM > historicalMax1RM -> {
                    announcedPRs.add(exerciseName)
                    _prEvent.emit(PrEvent(exerciseName, PrType.ONE_RM, current1RM, historicalMax1RM))
                }
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
