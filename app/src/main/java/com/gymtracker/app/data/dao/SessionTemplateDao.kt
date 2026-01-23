package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.model.SessionTemplate
import com.gymtracker.app.data.model.TemplateWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionTemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: SessionTemplate): Long

    @Update
    suspend fun update(template: SessionTemplate)

    @Delete
    suspend fun delete(template: SessionTemplate)

    @Query("SELECT * FROM session_templates ORDER BY name")
    fun getAllTemplates(): Flow<List<SessionTemplate>>

    @Query("SELECT * FROM session_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Long): SessionTemplate?

    @Transaction
    @Query("SELECT * FROM session_templates ORDER BY name")
    fun getAllTemplatesWithExercises(): Flow<List<TemplateWithExercises>>

    @Transaction
    @Query("SELECT * FROM session_templates WHERE id = :templateId")
    suspend fun getTemplateWithExercises(templateId: Long): TemplateWithExercises?

    @Query("DELETE FROM session_templates")
    suspend fun deleteAll()
}
