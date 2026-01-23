package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.model.TemplateExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: TemplateExercise): Long

    @Update
    suspend fun update(exercise: TemplateExercise)

    @Delete
    suspend fun delete(exercise: TemplateExercise)

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex")
    fun getExercisesForTemplate(templateId: Long): Flow<List<TemplateExercise>>

    @Query("SELECT * FROM template_exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Long): TemplateExercise?

    @Query("SELECT MAX(orderIndex) FROM template_exercises WHERE templateId = :templateId")
    suspend fun getMaxOrderIndex(templateId: Long): Int?

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteAllExercisesForTemplate(templateId: Long)
}
