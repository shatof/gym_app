package com.gymtracker.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gym_settings")

/**
 * Couleurs de thème disponibles
 */
enum class ThemeColor(val displayName: String) {
    GREEN("Vert"),
    BLUE("Bleu"),
    PURPLE("Violet"),
    PINK("Rose"),
    ORANGE("Orange"),
    RED("Rouge"),
    TEAL("Turquoise")
}

/**
 * Gestionnaire des préférences de l'application
 */
class SettingsManager(private val context: Context) {

    companion object {
        private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        private val THEME_COLOR = stringPreferencesKey("theme_color")
        private val COMMON_EXERCISES = stringPreferencesKey("common_exercises")
        private val WELCOME_TEXT = stringPreferencesKey("welcome_text")
        private val WELCOME_IMAGE_URI = stringPreferencesKey("welcome_image_uri")

        // Texte d'accueil par défaut
        const val DEFAULT_WELCOME_TEXT = "let's gooooooo"

        // Exercices par défaut proposés
        val DEFAULT_EXERCISES = listOf(
            "Développé couché",
            "Squat",
            "Soulevé de terre",
            "Rowing barre",
            "Développé épaules",
            "Curl biceps",
            "Extension triceps",
            "Leg press"
        )

        // Grande liste d'exercices de musculation
        val ALL_EXERCISES = listOf(
            // Pectoraux
            "Développé couché",
            "Développé couché haltères",
            "Développé incliné",
            "Développé incliné haltères",
            "Développé décliné",
            "Écarté couché",
            "Écarté incliné",
            "Pec deck",
            "Poulie vis-à-vis",
            "Pompes",
            "Dips pectoraux",

            // Dos
            "Tractions",
            "Tractions supination",
            "Tractions prise neutre",
            "Rowing barre",
            "Rowing haltère",
            "Rowing T-bar",
            "Tirage vertical",
            "Tirage horizontal",
            "Tirage poitrine",
            "Pullover",
            "Soulevé de terre",
            "Shrugs",

            // Épaules
            "Développé épaules",
            "Développé militaire",
            "Développé Arnold",
            "Élévations latérales",
            "Élévations frontales",
            "Oiseau",
            "Face pull",
            "Rowing menton",

            // Biceps
            "Curl biceps barre",
            "Curl biceps haltères",
            "Curl marteau",
            "Curl pupitre",
            "Curl incliné",
            "Curl concentration",
            "Curl poulie basse",

            // Triceps
            "Extension triceps",
            "Extension triceps poulie",
            "Barre au front",
            "Dips triceps",
            "Kickback",
            "Extension nuque",
            "Pushdown corde",

            // Jambes
            "Squat",
            "Squat barre devant",
            "Hack squat",
            "Leg press",
            "Fentes",
            "Fentes marchées",
            "Leg extension",
            "Leg curl",
            "Leg curl assis",
            "Soulevé de terre jambes tendues",
            "Hip thrust",
            "Mollets debout",
            "Mollets assis",
            "Presse mollets",

            // Abdominaux
            "Crunch",
            "Crunch inversé",
            "Relevé de jambes",
            "Planche",
            "Russian twist",
            "Ab wheel",
            "Gainage latéral",

            // Autres
            "Burpees",
            "Kettlebell swing",
            "Clean and jerk",
            "Snatch",
            "Farmer walk"
        )

        // Groupes musculaires
        enum class MuscleGroup(val displayName: String) {
            CHEST("Pectoraux"),
            BACK("Dos"),
            SHOULDERS("Épaules"),
            BICEPS("Biceps"),
            TRICEPS("Triceps"),
            LEGS("Jambes"),
            ABS("Abdominaux"),
            OTHER("Autre")
        }

        // Mapping exercice -> groupe musculaire
        val EXERCISE_TO_MUSCLE_GROUP: Map<String, MuscleGroup> = mapOf(
            // Pectoraux
            "Développé couché" to MuscleGroup.CHEST,
            "Développé couché haltères" to MuscleGroup.CHEST,
            "Développé incliné" to MuscleGroup.CHEST,
            "Développé incliné haltères" to MuscleGroup.CHEST,
            "Développé décliné" to MuscleGroup.CHEST,
            "Écarté couché" to MuscleGroup.CHEST,
            "Écarté incliné" to MuscleGroup.CHEST,
            "Pec deck" to MuscleGroup.CHEST,
            "Poulie vis-à-vis" to MuscleGroup.CHEST,
            "Pompes" to MuscleGroup.CHEST,
            "Dips pectoraux" to MuscleGroup.CHEST,

            // Dos
            "Tractions" to MuscleGroup.BACK,
            "Tractions supination" to MuscleGroup.BACK,
            "Tractions prise neutre" to MuscleGroup.BACK,
            "Rowing barre" to MuscleGroup.BACK,
            "Rowing haltère" to MuscleGroup.BACK,
            "Rowing T-bar" to MuscleGroup.BACK,
            "Tirage vertical" to MuscleGroup.BACK,
            "Tirage horizontal" to MuscleGroup.BACK,
            "Tirage poitrine" to MuscleGroup.BACK,
            "Pullover" to MuscleGroup.BACK,
            "Soulevé de terre" to MuscleGroup.BACK,
            "Shrugs" to MuscleGroup.BACK,

            // Épaules
            "Développé épaules" to MuscleGroup.SHOULDERS,
            "Développé militaire" to MuscleGroup.SHOULDERS,
            "Développé Arnold" to MuscleGroup.SHOULDERS,
            "Élévations latérales" to MuscleGroup.SHOULDERS,
            "Élévations frontales" to MuscleGroup.SHOULDERS,
            "Oiseau" to MuscleGroup.SHOULDERS,
            "Face pull" to MuscleGroup.SHOULDERS,
            "Rowing menton" to MuscleGroup.SHOULDERS,

            // Biceps
            "Curl biceps barre" to MuscleGroup.BICEPS,
            "Curl biceps haltères" to MuscleGroup.BICEPS,
            "Curl marteau" to MuscleGroup.BICEPS,
            "Curl pupitre" to MuscleGroup.BICEPS,
            "Curl incliné" to MuscleGroup.BICEPS,
            "Curl concentration" to MuscleGroup.BICEPS,
            "Curl poulie basse" to MuscleGroup.BICEPS,
            "Curl biceps" to MuscleGroup.BICEPS,

            // Triceps
            "Extension triceps" to MuscleGroup.TRICEPS,
            "Extension triceps poulie" to MuscleGroup.TRICEPS,
            "Barre au front" to MuscleGroup.TRICEPS,
            "Dips triceps" to MuscleGroup.TRICEPS,
            "Kickback" to MuscleGroup.TRICEPS,
            "Extension nuque" to MuscleGroup.TRICEPS,
            "Pushdown corde" to MuscleGroup.TRICEPS,

            // Jambes
            "Squat" to MuscleGroup.LEGS,
            "Squat barre devant" to MuscleGroup.LEGS,
            "Hack squat" to MuscleGroup.LEGS,
            "Leg press" to MuscleGroup.LEGS,
            "Fentes" to MuscleGroup.LEGS,
            "Fentes marchées" to MuscleGroup.LEGS,
            "Leg extension" to MuscleGroup.LEGS,
            "Leg curl" to MuscleGroup.LEGS,
            "Leg curl assis" to MuscleGroup.LEGS,
            "Soulevé de terre jambes tendues" to MuscleGroup.LEGS,
            "Hip thrust" to MuscleGroup.LEGS,
            "Mollets debout" to MuscleGroup.LEGS,
            "Mollets assis" to MuscleGroup.LEGS,
            "Presse mollets" to MuscleGroup.LEGS,

            // Abdominaux
            "Crunch" to MuscleGroup.ABS,
            "Crunch inversé" to MuscleGroup.ABS,
            "Relevé de jambes" to MuscleGroup.ABS,
            "Planche" to MuscleGroup.ABS,
            "Russian twist" to MuscleGroup.ABS,
            "Ab wheel" to MuscleGroup.ABS,
            "Gainage latéral" to MuscleGroup.ABS
        )

        /**
         * Récupère le groupe musculaire d'un exercice
         */
        fun getMuscleGroup(exerciseName: String): MuscleGroup {
            // Cherche d'abord une correspondance exacte
            EXERCISE_TO_MUSCLE_GROUP[exerciseName]?.let { return it }

            // Cherche une correspondance partielle (ignorer la casse)
            val lowerName = exerciseName.lowercase()
            for ((name, group) in EXERCISE_TO_MUSCLE_GROUP) {
                if (lowerName.contains(name.lowercase()) || name.lowercase().contains(lowerName)) {
                    return group
                }
            }

            // Détection par mots-clés
            return when {
                lowerName.contains("pec") || lowerName.contains("développé couché") || lowerName.contains("dips pec") -> MuscleGroup.CHEST
                lowerName.contains("traction") || lowerName.contains("rowing") || lowerName.contains("tirage") || lowerName.contains("dos") -> MuscleGroup.BACK
                lowerName.contains("épaule") || lowerName.contains("latéral") || lowerName.contains("militaire") -> MuscleGroup.SHOULDERS
                lowerName.contains("bicep") || lowerName.contains("curl") -> MuscleGroup.BICEPS
                lowerName.contains("tricep") || lowerName.contains("extension") || lowerName.contains("pushdown") -> MuscleGroup.TRICEPS
                lowerName.contains("squat") || lowerName.contains("leg") || lowerName.contains("jambe") || lowerName.contains("fente") || lowerName.contains("mollet") || lowerName.contains("hip thrust") -> MuscleGroup.LEGS
                lowerName.contains("abdos") || lowerName.contains("crunch") || lowerName.contains("planche") || lowerName.contains("gainage") -> MuscleGroup.ABS
                else -> MuscleGroup.OTHER
            }
        }
    }

    // Thème sombre
    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_THEME] ?: true
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }

    // Couleur du thème
    val themeColor: Flow<ThemeColor> = context.dataStore.data.map { preferences ->
        val colorName = preferences[THEME_COLOR] ?: ThemeColor.GREEN.name
        try {
            ThemeColor.valueOf(colorName)
        } catch (e: Exception) {
            ThemeColor.GREEN
        }
    }

    suspend fun setThemeColor(color: ThemeColor) {
        context.dataStore.edit { preferences ->
            preferences[THEME_COLOR] = color.name
        }
    }

    // Exercices courants personnalisés
    val commonExercises: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val exercisesJson = preferences[COMMON_EXERCISES]
        if (exercisesJson != null) {
            exercisesJson.split("|||").filter { it.isNotBlank() }
        } else {
            DEFAULT_EXERCISES
        }
    }

    suspend fun setCommonExercises(exercises: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[COMMON_EXERCISES] = exercises.joinToString("|||")
        }
    }

    suspend fun addCommonExercise(exercise: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[COMMON_EXERCISES]?.split("|||")?.filter { it.isNotBlank() }
                ?: DEFAULT_EXERCISES
            if (!current.contains(exercise)) {
                preferences[COMMON_EXERCISES] = (current + exercise).joinToString("|||")
            }
        }
    }

    suspend fun removeCommonExercise(exercise: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[COMMON_EXERCISES]?.split("|||")?.filter { it.isNotBlank() }
                ?: DEFAULT_EXERCISES
            preferences[COMMON_EXERCISES] = current.filter { it != exercise }.joinToString("|||")
        }
    }

    // Texte d'accueil personnalisé
    val welcomeText: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WELCOME_TEXT] ?: DEFAULT_WELCOME_TEXT
    }

    suspend fun setWelcomeText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[WELCOME_TEXT] = text.ifBlank { DEFAULT_WELCOME_TEXT }
        }
    }

    // Image d'accueil personnalisée (URI stocké en string)
    val welcomeImageUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[WELCOME_IMAGE_URI]
    }

    suspend fun setWelcomeImageUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[WELCOME_IMAGE_URI] = uri
            } else {
                preferences.remove(WELCOME_IMAGE_URI)
            }
        }
    }
}
