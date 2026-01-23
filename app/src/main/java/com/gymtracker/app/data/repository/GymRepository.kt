package com.gymtracker.app.data.repository

import com.gymtracker.app.data.dao.ExerciseDao
import com.gymtracker.app.data.dao.ExerciseSetDao
import com.gymtracker.app.data.dao.SessionTemplateDao
import com.gymtracker.app.data.dao.TemplateExerciseDao
import com.gymtracker.app.data.dao.WorkoutDao
import com.gymtracker.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GymRepository(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val exerciseSetDao: ExerciseSetDao,
    private val sessionTemplateDao: SessionTemplateDao,
    private val templateExerciseDao: TemplateExerciseDao
) {
    // Workouts
    val allWorkouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()
    val allWorkoutsWithExercises: Flow<List<WorkoutWithExercises>> = workoutDao.getAllWorkoutsWithExercises()
    val completedWorkoutsWithExercises: Flow<List<WorkoutWithExercises>> = workoutDao.getCompletedWorkoutsWithExercises()
    
    suspend fun createWorkout(name: String = ""): Long {
        val workout = Workout(name = name)
        return workoutDao.insert(workout)
    }
    
    suspend fun updateWorkout(workout: Workout) = workoutDao.update(workout)
    
    suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)
    
    suspend fun getWorkoutById(id: Long) = workoutDao.getWorkoutById(id)
    
    suspend fun getWorkoutWithExercises(id: Long) = workoutDao.getWorkoutWithExercises(id)
    
    suspend fun getActiveWorkout() = workoutDao.getActiveWorkout()
    
    suspend fun completeWorkout(workoutId: Long, duration: Int, notes: String = "") {
        workoutDao.completeWorkout(workoutId, duration, notes)
    }
    
    // Exercises
    fun getExercisesWithSetsForWorkout(workoutId: Long): Flow<List<ExerciseWithSets>> =
        exerciseDao.getExercisesWithSetsForWorkout(workoutId)
    
    val allExerciseNames: Flow<List<String>> = exerciseDao.getAllExerciseNames()
    
    suspend fun addExercise(workoutId: Long, name: String, restTimeSeconds: Int = 180): Long {
        val maxOrder = exerciseDao.getMaxOrderIndex(workoutId) ?: -1
        val exercise = Exercise(
            workoutId = workoutId,
            name = name,
            orderIndex = maxOrder + 1,
            restTimeSeconds = restTimeSeconds
        )
        return exerciseDao.insert(exercise)
    }
    
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)
    
    // Sets
    fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>> =
        exerciseSetDao.getSetsForExercise(exerciseId)
    
    suspend fun addSet(exerciseId: Long, reps: Int = 0, weight: Float = 0f, miorep: Int? = null): Long {
        val maxSetNumber = exerciseSetDao.getMaxSetNumber(exerciseId) ?: 0
        val set = ExerciseSet(
            exerciseId = exerciseId,
            setNumber = maxSetNumber + 1,
            reps = reps,
            weight = weight,
            miorep = miorep
        )
        return exerciseSetDao.insert(set)
    }
    
    suspend fun updateSet(set: ExerciseSet) = exerciseSetDao.update(set)
    
    suspend fun updateSetValues(setId: Long, reps: Int, weight: Float, miorep: Int?) {
        exerciseSetDao.updateSetValues(setId, reps, weight, miorep)
    }
    
    suspend fun toggleSetCompletion(setId: Long, completed: Boolean) {
        exerciseSetDao.updateSetCompletion(setId, completed)
    }
    
    suspend fun deleteSet(set: ExerciseSet) = exerciseSetDao.delete(set)
    
    // Progress Data
    fun getProgressForExercise(exerciseName: String): Flow<ExerciseProgress> {
        return completedWorkoutsWithExercises.map { workouts ->
            val dataPoints = workouts
                .flatMap { workout ->
                    workout.exercises
                        .filter { it.exercise.name.equals(exerciseName, ignoreCase = true) }
                        .map { exerciseWithSets ->
                            val sets = exerciseWithSets.sets.filter { it.isCompleted }
                            if (sets.isNotEmpty()) {
                                val maxWeight = sets.maxOf { it.weight }
                                val totalVolume = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                                val bestSet = sets.maxByOrNull { calculate1RM(it.weight, it.reps) }
                                
                                ProgressDataPoint(
                                    date = workout.workout.date,
                                    maxWeight = maxWeight,
                                    totalVolume = totalVolume,
                                    bestSet = BestSetInfo(
                                        weight = bestSet?.weight ?: 0f,
                                        reps = bestSet?.reps ?: 0,
                                        estimated1RM = bestSet?.let { calculate1RM(it.weight, it.reps) } ?: 0f
                                    )
                                )
                            } else null
                        }
                        .filterNotNull()
                }
                .sortedBy { it.date }
            
            ExerciseProgress(
                exerciseName = exerciseName,
                dataPoints = dataPoints
            )
        }
    }
    
    // Export
    suspend fun exportAllData(): ExportData {
        val workouts = allWorkoutsWithExercises.first()
        return ExportData(
            workouts = workouts.map { it.toExport() }
        )
    }

    // === Templates ===

    val allTemplates: Flow<List<SessionTemplate>> = sessionTemplateDao.getAllTemplates()
    val allTemplatesWithExercises: Flow<List<TemplateWithExercises>> = sessionTemplateDao.getAllTemplatesWithExercises()

    suspend fun createTemplate(name: String, description: String = ""): Long {
        val template = SessionTemplate(name = name, description = description)
        return sessionTemplateDao.insert(template)
    }

    suspend fun updateTemplate(template: SessionTemplate) = sessionTemplateDao.update(template)

    suspend fun deleteTemplate(template: SessionTemplate) = sessionTemplateDao.delete(template)

    suspend fun getTemplateById(id: Long) = sessionTemplateDao.getTemplateById(id)

    suspend fun getTemplateWithExercises(id: Long) = sessionTemplateDao.getTemplateWithExercises(id)

    // Template Exercises
    suspend fun addTemplateExercise(templateId: Long, name: String, defaultSetsCount: Int = 3, restTimeSeconds: Int = 180): Long {
        val maxOrder = templateExerciseDao.getMaxOrderIndex(templateId) ?: -1
        val exercise = TemplateExercise(
            templateId = templateId,
            name = name,
            orderIndex = maxOrder + 1,
            defaultSetsCount = defaultSetsCount,
            restTimeSeconds = restTimeSeconds
        )
        return templateExerciseDao.insert(exercise)
    }

    suspend fun updateTemplateExercise(exercise: TemplateExercise) = templateExerciseDao.update(exercise)

    suspend fun deleteTemplateExercise(exercise: TemplateExercise) = templateExerciseDao.delete(exercise)

    fun getExercisesForTemplate(templateId: Long): Flow<List<TemplateExercise>> =
        templateExerciseDao.getExercisesForTemplate(templateId)

    // === Créer une séance à partir d'un template ===

    suspend fun createWorkoutFromTemplate(templateId: Long): Long {
        val template = sessionTemplateDao.getTemplateWithExercises(templateId)
            ?: throw IllegalArgumentException("Template not found")

        // Créer la séance avec le nom du template
        val workoutId = workoutDao.insert(Workout(name = template.template.name))

        // Ajouter les exercices du template à la séance
        for (templateExercise in template.exercises.sortedBy { it.orderIndex }) {
            val exerciseId = exerciseDao.insert(
                Exercise(
                    workoutId = workoutId,
                    name = templateExercise.name,
                    orderIndex = templateExercise.orderIndex,
                    restTimeSeconds = templateExercise.restTimeSeconds
                )
            )

            // Récupérer TOUTES les séries de la dernière séance pour cet exercice
            val lastSets = exerciseSetDao.getAllSetsFromLastWorkoutByExerciseName(templateExercise.name)

            if (lastSets.isNotEmpty()) {
                // Utiliser les séries de la dernière séance
                lastSets.forEachIndexed { index, lastSet ->
                    exerciseSetDao.insert(
                        ExerciseSet(
                            exerciseId = exerciseId,
                            setNumber = index + 1,
                            weight = lastSet.weight,
                            reps = lastSet.reps,
                            miorep = lastSet.miorep
                        )
                    )
                }
            } else {
                // Pas d'historique, créer des séries vides
                for (setNumber in 1..templateExercise.defaultSetsCount) {
                    exerciseSetDao.insert(
                        ExerciseSet(
                            exerciseId = exerciseId,
                            setNumber = setNumber,
                            weight = 0f,
                            reps = 0,
                            miorep = null
                        )
                    )
                }
            }
        }

        return workoutId
    }

    // === Récupérer les dernières valeurs d'un exercice ===

    suspend fun getLastSetValuesForExercise(exerciseName: String): ExerciseSet? {
        return exerciseSetDao.getLastCompletedSetByExerciseName(exerciseName)
    }

    suspend fun getAllSetsFromLastWorkout(exerciseName: String): List<ExerciseSet> {
        return exerciseSetDao.getAllSetsFromLastWorkoutByExerciseName(exerciseName)
    }

    // === Supprimer toutes les données ===

    suspend fun deleteAllData() {
        // Supprimer toutes les séances (les exercices et sets seront supprimés en cascade)
        workoutDao.deleteAll()
        // Supprimer tous les templates (les exercices de template seront supprimés en cascade)
        sessionTemplateDao.deleteAll()
    }

    // === Importer des données ===

    suspend fun importData(exportData: ExportData): Pair<Int, Int> {
        var workoutsImported = 0
        var exercisesImported = 0

        for (workoutExport in exportData.workouts) {
            // Créer la séance
            val workoutId = workoutDao.insert(
                Workout(
                    date = workoutExport.date,
                    name = workoutExport.name,
                    notes = workoutExport.notes,
                    durationMinutes = workoutExport.durationMinutes,
                    isCompleted = workoutExport.isCompleted
                )
            )
            workoutsImported++

            // Ajouter les exercices
            for (exerciseExport in workoutExport.exercises) {
                val exerciseId = exerciseDao.insert(
                    Exercise(
                        workoutId = workoutId,
                        name = exerciseExport.name,
                        orderIndex = exerciseExport.orderIndex
                    )
                )
                exercisesImported++

                // Ajouter les séries
                for (setExport in exerciseExport.sets) {
                    exerciseSetDao.insert(
                        ExerciseSet(
                            exerciseId = exerciseId,
                            setNumber = setExport.setNumber,
                            reps = setExport.reps,
                            weight = setExport.weight,
                            miorep = setExport.miorep,
                            isCompleted = setExport.isCompleted,
                            timestamp = setExport.timestamp
                        )
                    )
                }
            }
        }

        return Pair(workoutsImported, exercisesImported)
    }
}
