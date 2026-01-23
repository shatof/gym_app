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
