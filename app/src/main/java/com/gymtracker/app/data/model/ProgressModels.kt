package com.gymtracker.app.data.model

/**
 * Données pour les statistiques et graphiques de progression
 */
data class ExerciseProgress(
    val exerciseName: String,
    val dataPoints: List<ProgressDataPoint>
)

data class ProgressDataPoint(
    val date: Long,
    val maxWeight: Float,
    val totalVolume: Float, // poids x reps x séries
    val bestSet: BestSetInfo
)

data class BestSetInfo(
    val weight: Float,
    val reps: Int,
    val miorep: Int? = null,
    val estimated1RM: Float // 1RM estimé
)

/**
 * Calcule le nombre de répétitions effectives en incluant les mioreps
 * 1 miorep = 1/3 de rep
 */
fun calculateEffectiveReps(reps: Int, miorep: Int?): Float {
    return reps + (miorep ?: 0) / 3f
}

/**
 * Calcule le 1RM estimé avec la formule d'Epley
 * Prend en compte les mioreps (1 miorep = 1/3 de rep)
 */
fun calculate1RM(weight: Float, reps: Int, miorep: Int? = null): Float {
    val effectiveReps = calculateEffectiveReps(reps, miorep)
    return if (effectiveReps <= 1) weight
    else weight * (1 + effectiveReps / 30f)
}

// ────────────────────────────────────────────────
// Groupe A — nouvelles structures de données
// ────────────────────────────────────────────────

/** Événement émis quand un record personnel est battu en séance */
data class PrEvent(
    val exerciseName: String,
    val type: PrType,
    val newValue: Float,
    val previousValue: Float
)

enum class PrType(val label: String) {
    WEIGHT("poids"),
    ONE_RM("1RM estimé")
}

/** Suggestion de charge pour le prochain set d'un exercice */
data class LoadSuggestion(
    val suggestedWeight: Float,
    val reason: String,
    val isProgression: Boolean   // true = augmentation, false = maintien
)

/** Stats hebdomadaires pour les objectifs de la semaine */
data class WeeklyStats(
    val sessionsCount: Int,
    val totalMinutes: Int,
    val totalVolumeKg: Float,
    val sessionsGoal: Int = 4,
    val minutesGoal: Int = 120
)

/** Récapitulatif mensuel */
data class MonthlyStats(
    val monthName: String,
    val sessionsCount: Int,
    val totalMinutes: Int,
    val totalVolumeKg: Float,
    val uniqueExercises: Int
)
