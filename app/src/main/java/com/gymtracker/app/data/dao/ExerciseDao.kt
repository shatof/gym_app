package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.model.Exercise
import com.gymtracker.app.data.model.ExerciseWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise): Long
    
    @Update
    suspend fun update(exercise: Exercise)
    
    @Delete
    suspend fun delete(exercise: Exercise)
    
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY orderIndex")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<Exercise>>
    
    @Transaction
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY orderIndex")
    fun getExercisesWithSetsForWorkout(workoutId: Long): Flow<List<ExerciseWithSets>>
    
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Long): Exercise?
    
    @Query("SELECT DISTINCT name FROM exercises ORDER BY name")
    fun getAllExerciseNames(): Flow<List<String>>
    
    @Query("SELECT MAX(orderIndex) FROM exercises WHERE workoutId = :workoutId")
    suspend fun getMaxOrderIndex(workoutId: Long): Int?
}
