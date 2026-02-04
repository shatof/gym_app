package com.gymtracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Représente un exercice dans une séance
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val name: String,
    val orderIndex: Int = 0,
    val restTimeSeconds: Int = 180, // Temps de repos entre séries en secondes
    val supersetGroupId: Int? = null // ID du groupe superset (null = pas de superset)
)
