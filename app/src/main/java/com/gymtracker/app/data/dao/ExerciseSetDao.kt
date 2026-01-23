package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.model.ExerciseSet
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseSetDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exerciseSet: ExerciseSet): Long
    
    @Update
    suspend fun update(exerciseSet: ExerciseSet)
    
    @Delete
    suspend fun delete(exerciseSet: ExerciseSet)
    
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId ORDER BY setNumber")
    fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>>
    
    @Query("SELECT * FROM exercise_sets WHERE id = :setId")
    suspend fun getSetById(setId: Long): ExerciseSet?
    
    @Query("SELECT MAX(setNumber) FROM exercise_sets WHERE exerciseId = :exerciseId")
    suspend fun getMaxSetNumber(exerciseId: Long): Int?
    
    @Query("UPDATE exercise_sets SET isCompleted = :completed WHERE id = :setId")
    suspend fun updateSetCompletion(setId: Long, completed: Boolean)
    
    @Query("UPDATE exercise_sets SET reps = :reps, weight = :weight, miorep = :miorep WHERE id = :setId")
    suspend fun updateSetValues(setId: Long, reps: Int, weight: Float, miorep: Int?)
    
    @Query("""
        SELECT es.* FROM exercise_sets es
        INNER JOIN exercises e ON es.exerciseId = e.id
        INNER JOIN workouts w ON e.workoutId = w.id
        WHERE e.name = :exerciseName AND w.isCompleted = 1
        ORDER BY w.date DESC
    """)
    fun getSetsForExerciseByName(exerciseName: String): Flow<List<ExerciseSet>>
    
    @Query("DELETE FROM exercise_sets WHERE exerciseId = :exerciseId")
    suspend fun deleteAllSetsForExercise(exerciseId: Long)
}
