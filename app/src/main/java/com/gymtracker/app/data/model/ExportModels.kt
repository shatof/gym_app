package com.gymtracker.app.data.model

/**
 * Classes pour l'export JSON
 */
data class ExportData(
    val exportDate: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val workouts: List<WorkoutExport>,
    val measurements: List<MeasurementExport> = emptyList(),
    val unlockedBadges: Map<String, Long> = emptyMap()
)

data class MeasurementExport(
    val id: Long,
    val date: Long,
    val weight: Float?,
    val bodyFat: Float?,
    val armLeft: Float?,
    val armRight: Float?,
    val chest: Float?,
    val waist: Float?,
    val hips: Float?,
    val thighLeft: Float?,
    val thighRight: Float?,
    val calfLeft: Float?,
    val calfRight: Float?,
    val shoulders: Float?,
    val neck: Float?,
    val forearmLeft: Float?,
    val forearmRight: Float?,
    val notes: String
)

fun Measurement.toExport(): MeasurementExport {
    return MeasurementExport(
        id = id,
        date = date,
        weight = weight,
        bodyFat = bodyFat,
        armLeft = armLeft,
        armRight = armRight,
        chest = chest,
        waist = waist,
        hips = hips,
        thighLeft = thighLeft,
        thighRight = thighRight,
        calfLeft = calfLeft,
        calfRight = calfRight,
        shoulders = shoulders,
        neck = neck,
        forearmLeft = forearmLeft,
        forearmRight = forearmRight,
        notes = notes
    )
}

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

data class TemplateExportData(
    val exportDate: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val templates: List<TemplateExport>
)

data class TemplateExport(
    val name: String,
    val description: String,
    val createdAt: Long,
    val exercises: List<TemplateExerciseExport>
)

data class TemplateExerciseExport(
    val name: String,
    val orderIndex: Int,
    val defaultSetsCount: Int,
    val restTimeSeconds: Int,
    val supersetGroupId: Int?
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

fun TemplateWithExercises.toExport(): TemplateExport {
    return TemplateExport(
        name = template.name,
        description = template.description,
        createdAt = template.createdAt,
        exercises = exercises
            .sortedBy { it.orderIndex }
            .map { exercise ->
                TemplateExerciseExport(
                    name = exercise.name,
                    orderIndex = exercise.orderIndex,
                    defaultSetsCount = exercise.defaultSetsCount,
                    restTimeSeconds = exercise.restTimeSeconds,
                    supersetGroupId = exercise.supersetGroupId
                )
            }
    )
}

