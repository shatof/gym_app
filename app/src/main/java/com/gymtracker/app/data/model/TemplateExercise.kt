package com.gymtracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Représente un exercice dans un template de séance
 */
@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = SessionTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId")]
)
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val name: String,
    val orderIndex: Int = 0,
    val defaultSetsCount: Int = 3, // Nombre de séries par défaut
    val restTimeSeconds: Int = 180 // Temps de repos entre séries en secondes
)
