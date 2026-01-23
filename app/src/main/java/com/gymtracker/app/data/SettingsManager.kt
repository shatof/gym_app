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
