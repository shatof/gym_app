package com.gymtracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente un template de séance (ex: Push, Pull, Legs)
 * Un template contient une liste d'exercices prédéfinis
 */
@Entity(tableName = "session_templates")
data class SessionTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
