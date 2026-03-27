package com.gymtracker.app.data.model

/**
 * Représente un badge/accomplissement de l'utilisateur
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val category: BadgeCategory,
    val icon: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null,
    val progress: Float = 0f,  // 0.0 à 1.0
    val requirement: String
)

/**
 * Catégories de badges
 */
enum class BadgeCategory(val displayName: String) {
    STRENGTH("Force"),
    CONSISTENCY("Régularité"),
    MILESTONE("Paliers"),
    VOLUME("Volume"),
    SPECIAL("Spéciaux")
}

/**
 * Définitions de tous les badges disponibles
 */
object BadgeDefinitions {

    // === BADGES DE FORCE ===
    val SQUAT_60KG = BadgeDef(
        id = "squat_60kg",
        name = "Squat 60kg",
        description = "Atteindre 60kg au squat",
        category = BadgeCategory.STRENGTH,
        icon = "🦵",
        requirement = "Squat ≥ 60kg",
        checkCondition = { stats -> stats.maxWeightByExercise["Squat"]?.let { it >= 60f } ?: false }
    )

    val SQUAT_100KG = BadgeDef(
        id = "squat_100kg",
        name = "Squat 100kg",
        description = "Atteindre 100kg au squat",
        category = BadgeCategory.STRENGTH,
        icon = "🦵",
        requirement = "Squat ≥ 100kg",
        checkCondition = { stats -> stats.maxWeightByExercise["Squat"]?.let { it >= 100f } ?: false }
    )

    val SQUAT_140KG = BadgeDef(
        id = "squat_140kg",
        name = "Squat 140kg",
        description = "Atteindre 140kg au squat",
        category = BadgeCategory.STRENGTH,
        icon = "🏆",
        requirement = "Squat ≥ 140kg",
        checkCondition = { stats -> stats.maxWeightByExercise["Squat"]?.let { it >= 140f } ?: false }
    )

    val BENCH_40KG = BadgeDef(
        id = "bench_40kg",
        name = "Développé couché 40kg",
        description = "Atteindre 40kg au développé couché",
        category = BadgeCategory.STRENGTH,
        icon = "🏋️",
        requirement = "Développé couché ≥ 40kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Développé couché"]?.let { it >= 40f } ?: false
        }
    )

    val BENCH_60KG = BadgeDef(
        id = "bench_60kg",
        name = "Développé couché 60kg",
        description = "Atteindre 60kg au développé couché",
        category = BadgeCategory.STRENGTH,
        icon = "🏋️",
        requirement = "Développé couché ≥ 60kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Développé couché"]?.let { it >= 60f } ?: false
        }
    )

    val BENCH_80KG = BadgeDef(
        id = "bench_80kg",
        name = "Développé couché 80kg",
        description = "Atteindre 80kg au développé couché",
        category = BadgeCategory.STRENGTH,
        icon = "🏋️",
        requirement = "Développé couché ≥ 80kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Développé couché"]?.let { it >= 80f } ?: false
        }
    )

    val BENCH_100KG = BadgeDef(
        id = "bench_100kg",
        name = "Développé couché 100kg",
        description = "Atteindre 100kg au développé couché - Bienvenue au club !",
        category = BadgeCategory.STRENGTH,
        icon = "🏆",
        requirement = "Développé couché ≥ 100kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Développé couché"]?.let { it >= 100f } ?: false
        }
    )

    val DEADLIFT_60KG = BadgeDef(
        id = "deadlift_60kg",
        name = "Soulevé de terre 60kg",
        description = "Atteindre 60kg au soulevé de terre",
        category = BadgeCategory.STRENGTH,
        icon = "💪",
        requirement = "Soulevé de terre ≥ 60kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Soulevé de terre"]?.let { it >= 60f } ?: false
        }
    )

    val DEADLIFT_100KG = BadgeDef(
        id = "deadlift_100kg",
        name = "Soulevé de terre 100kg",
        description = "Atteindre 100kg au soulevé de terre",
        category = BadgeCategory.STRENGTH,
        icon = "💪",
        requirement = "Soulevé de terre ≥ 100kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Soulevé de terre"]?.let { it >= 100f } ?: false
        }
    )

    val DEADLIFT_150KG = BadgeDef(
        id = "deadlift_150kg",
        name = "Soulevé de terre 150kg",
        description = "Atteindre 150kg au soulevé de terre",
        category = BadgeCategory.STRENGTH,
        icon = "🏆",
        requirement = "Soulevé de terre ≥ 150kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Soulevé de terre"]?.let { it >= 150f } ?: false
        }
    )

    val PULLUP_BODYWEIGHT = BadgeDef(
        id = "pullup_bodyweight",
        name = "Premier pull-up lesté",
        description = "Faire des tractions avec du poids additionnel",
        category = BadgeCategory.STRENGTH,
        icon = "🎯",
        requirement = "Tractions avec poids > 0kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Tractions"]?.let { it > 0f } ?: false
        }
    )

    val PULLUP_20KG = BadgeDef(
        id = "pullup_20kg",
        name = "Tractions +20kg",
        description = "Faire des tractions avec 20kg de lest",
        category = BadgeCategory.STRENGTH,
        icon = "💪",
        requirement = "Tractions ≥ +20kg",
        checkCondition = { stats ->
            stats.maxWeightByExercise["Tractions"]?.let { it >= 20f } ?: false
        }
    )

    // === BADGES DE RÉGULARITÉ ===
    val FIRST_WORKOUT = BadgeDef(
        id = "first_workout",
        name = "Premier pas",
        description = "Terminer sa première séance",
        category = BadgeCategory.CONSISTENCY,
        icon = "🌟",
        requirement = "1 séance terminée",
        checkCondition = { stats -> stats.totalWorkouts >= 1 }
    )

    val WORKOUTS_5 = BadgeDef(
        id = "workouts_5",
        name = "Habitude naissante",
        description = "Terminer 5 séances",
        category = BadgeCategory.CONSISTENCY,
        icon = "⭐",
        requirement = "5 séances terminées",
        checkCondition = { stats -> stats.totalWorkouts >= 5 }
    )

    val WORKOUTS_10 = BadgeDef(
        id = "workouts_10",
        name = "C'est parti !",
        description = "Terminer 10 séances",
        category = BadgeCategory.CONSISTENCY,
        icon = "🔥",
        requirement = "10 séances terminées",
        checkCondition = { stats -> stats.totalWorkouts >= 10 }
    )

    val WORKOUTS_25 = BadgeDef(
        id = "workouts_25",
        name = "Motivé",
        description = "Terminer 25 séances",
        category = BadgeCategory.CONSISTENCY,
        icon = "💪",
        requirement = "25 séances terminées",
        checkCondition = { stats -> stats.totalWorkouts >= 25 }
    )

    val WORKOUTS_50 = BadgeDef(
        id = "workouts_50",
        name = "Demi-centenaire",
        description = "Terminer 50 séances",
        category = BadgeCategory.CONSISTENCY,
        icon = "🏅",
        requirement = "50 séances terminées",
        checkCondition = { stats -> stats.totalWorkouts >= 50 }
    )

    val WORKOUTS_100 = BadgeDef(
        id = "workouts_100",
        name = "Centurion",
        description = "Terminer 100 séances",
        category = BadgeCategory.CONSISTENCY,
        icon = "🏆",
        requirement = "100 séances terminées",
        checkCondition = { stats -> stats.totalWorkouts >= 100 }
    )

    val STREAK_3 = BadgeDef(
        id = "streak_3",
        name = "En route",
        description = "3 séances d'affilée (sans pause > 3 jours)",
        category = BadgeCategory.CONSISTENCY,
        icon = "🔥",
        requirement = "3 séances consécutives",
        checkCondition = { stats -> stats.currentStreak >= 3 }
    )

    val STREAK_5 = BadgeDef(
        id = "streak_5",
        name = "Sur sa lancée",
        description = "5 séances d'affilée",
        category = BadgeCategory.CONSISTENCY,
        icon = "🔥",
        requirement = "5 séances consécutives",
        checkCondition = { stats -> stats.currentStreak >= 5 }
    )

    val STREAK_10 = BadgeDef(
        id = "streak_10",
        name = "Machine",
        description = "10 séances d'affilée",
        category = BadgeCategory.CONSISTENCY,
        icon = "⚡",
        requirement = "10 séances consécutives",
        checkCondition = { stats -> stats.currentStreak >= 10 }
    )

    val STREAK_20 = BadgeDef(
        id = "streak_20",
        name = "Inarrêtable",
        description = "20 séances d'affilée",
        category = BadgeCategory.CONSISTENCY,
        icon = "💥",
        requirement = "20 séances consécutives",
        checkCondition = { stats -> stats.currentStreak >= 20 }
    )

    // === BADGES DE PALIERS / MILESTONES ===
    val FIRST_PR = BadgeDef(
        id = "first_pr",
        name = "Premier record !",
        description = "Battre un record personnel pour la première fois",
        category = BadgeCategory.MILESTONE,
        icon = "🎉",
        requirement = "1 PR battu",
        checkCondition = { stats -> stats.totalPRs >= 1 }
    )

    val PR_5 = BadgeDef(
        id = "pr_5",
        name = "Progression constante",
        description = "Battre 5 records personnels",
        category = BadgeCategory.MILESTONE,
        icon = "📈",
        requirement = "5 PRs battus",
        checkCondition = { stats -> stats.totalPRs >= 5 }
    )

    val PR_10 = BadgeDef(
        id = "pr_10",
        name = "Sur la bonne voie",
        description = "Battre 10 records personnels",
        category = BadgeCategory.MILESTONE,
        icon = "🚀",
        requirement = "10 PRs battus",
        checkCondition = { stats -> stats.totalPRs >= 10 }
    )

    val PR_25 = BadgeDef(
        id = "pr_25",
        name = "Évolution",
        description = "Battre 25 records personnels",
        category = BadgeCategory.MILESTONE,
        icon = "🌟",
        requirement = "25 PRs battus",
        checkCondition = { stats -> stats.totalPRs >= 25 }
    )

    // === BADGES DE VOLUME ===
    val VOLUME_1000 = BadgeDef(
        id = "volume_1000",
        name = "Première tonne",
        description = "Soulever 1 000 kg au total en une séance",
        category = BadgeCategory.VOLUME,
        icon = "📦",
        requirement = "Volume ≥ 1 000 kg/séance",
        checkCondition = { stats -> stats.maxVolumeInWorkout >= 1000f }
    )

    val VOLUME_5000 = BadgeDef(
        id = "volume_5000",
        name = "5 tonnes",
        description = "Soulever 5 000 kg au total en une séance",
        category = BadgeCategory.VOLUME,
        icon = "🏗️",
        requirement = "Volume ≥ 5 000 kg/séance",
        checkCondition = { stats -> stats.maxVolumeInWorkout >= 5000f }
    )

    val VOLUME_10000 = BadgeDef(
        id = "volume_10000",
        name = "10 tonnes",
        description = "Soulever 10 000 kg au total en une séance",
        category = BadgeCategory.VOLUME,
        icon = "🏋️",
        requirement = "Volume ≥ 10 000 kg/séance",
        checkCondition = { stats -> stats.maxVolumeInWorkout >= 10000f }
    )

    val TOTAL_VOLUME_100K = BadgeDef(
        id = "total_volume_100k",
        name = "100 tonnes soulevées",
        description = "Soulever 100 000 kg au total depuis le début",
        category = BadgeCategory.VOLUME,
        icon = "🏔️",
        requirement = "Volume total ≥ 100 000 kg",
        checkCondition = { stats -> stats.totalVolume >= 100000f }
    )

    val TOTAL_VOLUME_500K = BadgeDef(
        id = "total_volume_500k",
        name = "500 tonnes soulevées",
        description = "Soulever 500 000 kg au total",
        category = BadgeCategory.VOLUME,
        icon = "🌋",
        requirement = "Volume total ≥ 500 000 kg",
        checkCondition = { stats -> stats.totalVolume >= 500000f }
    )

    val TOTAL_VOLUME_1M = BadgeDef(
        id = "total_volume_1m",
        name = "Un million de kg",
        description = "Soulever 1 000 000 kg au total - Légendaire !",
        category = BadgeCategory.VOLUME,
        icon = "👑",
        requirement = "Volume total ≥ 1 000 000 kg",
        checkCondition = { stats -> stats.totalVolume >= 1000000f }
    )

    // === BADGES SPÉCIAUX ===
    val EARLY_BIRD = BadgeDef(
        id = "early_bird",
        name = "Lève-tôt",
        description = "Terminer une séance avant 7h du matin",
        category = BadgeCategory.SPECIAL,
        icon = "🌅",
        requirement = "Séance terminée avant 7h",
        checkCondition = { stats -> stats.hasEarlyMorningWorkout }
    )

    val NIGHT_OWL = BadgeDef(
        id = "night_owl",
        name = "Oiseau de nuit",
        description = "Terminer une séance après 22h",
        category = BadgeCategory.SPECIAL,
        icon = "🌙",
        requirement = "Séance terminée après 22h",
        checkCondition = { stats -> stats.hasLateNightWorkout }
    )

    val VARIETY = BadgeDef(
        id = "variety",
        name = "Polyvalent",
        description = "Pratiquer 10 exercices différents",
        category = BadgeCategory.SPECIAL,
        icon = "🎨",
        requirement = "10 exercices différents",
        checkCondition = { stats -> stats.uniqueExercises >= 10 }
    )

    val VARIETY_20 = BadgeDef(
        id = "variety_20",
        name = "Touche-à-tout",
        description = "Pratiquer 20 exercices différents",
        category = BadgeCategory.SPECIAL,
        icon = "🌈",
        requirement = "20 exercices différents",
        checkCondition = { stats -> stats.uniqueExercises >= 20 }
    )

    val FULL_BODY = BadgeDef(
        id = "full_body",
        name = "Full body",
        description = "Travailler tous les groupes musculaires dans une même séance",
        category = BadgeCategory.SPECIAL,
        icon = "💯",
        requirement = "Tous les groupes musculaires en 1 séance",
        checkCondition = { stats -> stats.hasFullBodyWorkout }
    )

    /**
     * Liste de tous les badges
     */
    val ALL_BADGES = listOf(
        // Force
        SQUAT_60KG, SQUAT_100KG, SQUAT_140KG,
        BENCH_40KG, BENCH_60KG, BENCH_80KG, BENCH_100KG,
        DEADLIFT_60KG, DEADLIFT_100KG, DEADLIFT_150KG,
        PULLUP_BODYWEIGHT, PULLUP_20KG,
        // Régularité
        FIRST_WORKOUT, WORKOUTS_5, WORKOUTS_10, WORKOUTS_25, WORKOUTS_50, WORKOUTS_100,
        STREAK_3, STREAK_5, STREAK_10, STREAK_20,
        // Paliers
        FIRST_PR, PR_5, PR_10, PR_25,
        // Volume
        VOLUME_1000, VOLUME_5000, VOLUME_10000,
        TOTAL_VOLUME_100K, TOTAL_VOLUME_500K, TOTAL_VOLUME_1M,
        // Spéciaux
        EARLY_BIRD, NIGHT_OWL, VARIETY, VARIETY_20, FULL_BODY
    )
}

/**
 * Définition d'un badge avec sa condition de déverrouillage
 */
data class BadgeDef(
    val id: String,
    val name: String,
    val description: String,
    val category: BadgeCategory,
    val icon: String,
    val requirement: String,
    val checkCondition: (UserStats) -> Boolean
) {
    fun toBadge(stats: UserStats, unlockedBadges: Map<String, Long>): Badge {
        val isUnlocked = checkCondition(stats)
        return Badge(
            id = id,
            name = name,
            description = description,
            category = category,
            icon = icon,
            isUnlocked = isUnlocked || unlockedBadges.containsKey(id),
            unlockedDate = unlockedBadges[id],
            progress = calculateProgress(stats),
            requirement = requirement
        )
    }

    private fun calculateProgress(stats: UserStats): Float {
        // Calcul simplifié de la progression pour certains badges
        return when {
            checkCondition(stats) -> 1f
            id.startsWith("workouts_") -> {
                val target = id.substringAfter("workouts_").toIntOrNull() ?: 1
                (stats.totalWorkouts.toFloat() / target).coerceIn(0f, 1f)
            }
            id.startsWith("streak_") -> {
                val target = id.substringAfter("streak_").toIntOrNull() ?: 1
                (stats.currentStreak.toFloat() / target).coerceIn(0f, 1f)
            }
            id.startsWith("pr_") -> {
                val target = id.substringAfter("pr_").toIntOrNull() ?: 1
                (stats.totalPRs.toFloat() / target).coerceIn(0f, 1f)
            }
            else -> 0f
        }
    }
}

/**
 * Statistiques de l'utilisateur pour vérifier les conditions de badges
 */
data class UserStats(
    val totalWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val totalPRs: Int = 0,
    val maxWeightByExercise: Map<String, Float> = emptyMap(),
    val maxVolumeInWorkout: Float = 0f,
    val totalVolume: Float = 0f,
    val uniqueExercises: Int = 0,
    val hasEarlyMorningWorkout: Boolean = false,
    val hasLateNightWorkout: Boolean = false,
    val hasFullBodyWorkout: Boolean = false
)

