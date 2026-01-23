package com.gymtracker.app.data.repository

import com.gymtracker.app.data.dao.ExerciseDao
import com.gymtracker.app.data.dao.ExerciseSetDao
import com.gymtracker.app.data.dao.WorkoutDao
import com.gymtracker.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GymRepository(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val exerciseSetDao: ExerciseSetDao
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
    
    suspend fun completeWorkout(workoutId: Long, duration: Int) {
        workoutDao.completeWorkout(workoutId, duration)
    }
    
    // Exercises
    fun getExercisesWithSetsForWorkout(workoutId: Long): Flow<List<ExerciseWithSets>> =
        exerciseDao.getExercisesWithSetsForWorkout(workoutId)
    
    val allExerciseNames: Flow<List<String>> = exerciseDao.getAllExerciseNames()
    
    suspend fun addExercise(workoutId: Long, name: String): Long {
        val maxOrder = exerciseDao.getMaxOrderIndex(workoutId) ?: -1
        val exercise = Exercise(
            workoutId = workoutId,
            name = name,
            orderIndex = maxOrder + 1
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
}
