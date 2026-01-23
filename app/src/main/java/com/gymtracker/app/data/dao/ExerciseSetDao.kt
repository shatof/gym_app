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

    /**
     * Récupère les dernières valeurs (poids, reps, miorep) pour un exercice donné
     * basé sur les séances complétées
     */
    @Query("""
        SELECT es.* FROM exercise_sets es
        INNER JOIN exercises e ON es.exerciseId = e.id
        INNER JOIN workouts w ON e.workoutId = w.id
        WHERE e.name = :exerciseName 
        AND w.isCompleted = 1 
        AND es.isCompleted = 1
        ORDER BY w.date DESC, es.setNumber DESC
        LIMIT 1
    """)
    suspend fun getLastCompletedSetByExerciseName(exerciseName: String): ExerciseSet?

    /**
     * Récupère TOUTES les séries de la dernière séance complétée pour un exercice donné
     */
    @Query("""
        SELECT es.* FROM exercise_sets es
        INNER JOIN exercises e ON es.exerciseId = e.id
        INNER JOIN workouts w ON e.workoutId = w.id
        WHERE e.name = :exerciseName 
        AND w.isCompleted = 1
        AND w.id = (
            SELECT w2.id FROM workouts w2
            INNER JOIN exercises e2 ON e2.workoutId = w2.id
            WHERE e2.name = :exerciseName AND w2.isCompleted = 1
            ORDER BY w2.date DESC
            LIMIT 1
        )
        ORDER BY es.setNumber ASC
    """)
    suspend fun getAllSetsFromLastWorkoutByExerciseName(exerciseName: String): List<ExerciseSet>
}
