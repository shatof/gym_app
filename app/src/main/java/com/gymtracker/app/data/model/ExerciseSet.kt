package com.gymtracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Représente une série d'un exercice
 */
@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val setNumber: Int,
    val reps: Int = 0,
    val weight: Float = 0f,
    val miorep: Int? = null, // Miorep optionnel
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
