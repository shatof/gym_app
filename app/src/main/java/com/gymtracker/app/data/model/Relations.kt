package com.gymtracker.app.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation entre un exercice et ses séries
 */
data class ExerciseWithSets(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val sets: List<ExerciseSet>
)

/**
 * Relation entre une séance et ses exercices (avec leurs séries)
 */
data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        entity = Exercise::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<ExerciseWithSets>
)

/**
 * Relation entre un template et ses exercices
 */
data class TemplateWithExercises(
    @Embedded val template: SessionTemplate,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val exercises: List<TemplateExercise>
)

/**
 * Données pour récupérer les dernières valeurs d'un exercice
 */
data class LastExerciseValues(
    val exerciseName: String,
    val weight: Float,
    val reps: Int,
    val miorep: Int?
)

