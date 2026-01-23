package com.gymtracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente une séance d'entraînement complète
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val name: String = "",
    val notes: String = "",
    val durationMinutes: Int = 0,
    val isCompleted: Boolean = false
)
