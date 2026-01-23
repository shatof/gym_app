package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.model.Workout
import com.gymtracker.app.data.model.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: Workout): Long
    
    @Update
    suspend fun update(workout: Workout)
    
    @Delete
    suspend fun delete(workout: Workout)
    
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<Workout>>
    
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Long): Workout?
    
    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutWithExercises(workoutId: Long): WorkoutWithExercises?
    
    @Transaction
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>
    
    @Transaction
    @Query("SELECT * FROM workouts WHERE isCompleted = 1 ORDER BY date DESC")
    fun getCompletedWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>
    
    @Query("SELECT * FROM workouts WHERE isCompleted = 0 LIMIT 1")
    suspend fun getActiveWorkout(): Workout?
    
    @Query("UPDATE workouts SET isCompleted = 1, durationMinutes = :duration WHERE id = :workoutId")
    suspend fun completeWorkout(workoutId: Long, duration: Int)
}
