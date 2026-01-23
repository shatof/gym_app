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
    val estimated1RM: Float // 1RM estimé
)

/**
 * Calcule le 1RM estimé avec la formule d'Epley
 */
fun calculate1RM(weight: Float, reps: Int): Float {
    return if (reps == 1) weight
    else weight * (1 + reps / 30f)
}
