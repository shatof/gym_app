package com.gymtracker.app.data.model

/**
 * Classes pour l'export JSON
 */
data class ExportData(
    val exportDate: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val workouts: List<WorkoutExport>
)

data class WorkoutExport(
    val id: Long,
    val date: Long,
    val name: String,
    val notes: String,
    val durationMinutes: Int,
    val isCompleted: Boolean,
    val exercises: List<ExerciseExport>
)

data class ExerciseExport(
    val id: Long,
    val name: String,
    val orderIndex: Int,
    val sets: List<SetExport>
)

data class SetExport(
    val setNumber: Int,
    val reps: Int,
    val weight: Float,
    val miorep: Int?,
    val isCompleted: Boolean,
    val timestamp: Long
)

/**
 * Extension pour convertir les données internes en format d'export
 */
fun WorkoutWithExercises.toExport(): WorkoutExport {
    return WorkoutExport(
        id = workout.id,
        date = workout.date,
        name = workout.name,
        notes = workout.notes,
        durationMinutes = workout.durationMinutes,
        isCompleted = workout.isCompleted,
        exercises = exercises.map { exerciseWithSets ->
            ExerciseExport(
                id = exerciseWithSets.exercise.id,
                name = exerciseWithSets.exercise.name,
                orderIndex = exerciseWithSets.exercise.orderIndex,
                sets = exerciseWithSets.sets.map { set ->
                    SetExport(
                        setNumber = set.setNumber,
                        reps = set.reps,
                        weight = set.weight,
                        miorep = set.miorep,
                        isCompleted = set.isCompleted,
                        timestamp = set.timestamp
                    )
                }
            )
        }
    )
}
