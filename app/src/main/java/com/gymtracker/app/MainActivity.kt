package com.gymtracker.app

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gymtracker.app.ui.navigation.GymTrackerNavigation
import com.gymtracker.app.ui.theme.Background
import com.gymtracker.app.ui.theme.GymTrackerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GymTrackerApp

        // Gérer l'action du raccourci entrant
        val shortcutAction = intent.action?.takeIf {
            it == GymTrackerApp.ACTION_FREE_WORKOUT || it == GymTrackerApp.ACTION_START_TEMPLATE
        }
        val templateId = intent.getLongExtra(GymTrackerApp.EXTRA_TEMPLATE_ID, -1L).takeIf { it != -1L }
        if (shortcutAction != null) {
            app.pendingShortcutAction.value = Pair(shortcutAction, templateId)
        }

        // Créer les raccourcis dynamiques
        createDynamicShortcuts(app)

        setContent {
            GymTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    GymTrackerNavigation(repository = app.repository)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val app = application as GymTrackerApp
        val shortcutAction = intent.action?.takeIf {
            it == GymTrackerApp.ACTION_FREE_WORKOUT || it == GymTrackerApp.ACTION_START_TEMPLATE
        }
        val templateId = intent.getLongExtra(GymTrackerApp.EXTRA_TEMPLATE_ID, -1L).takeIf { it != -1L }
        if (shortcutAction != null) {
            app.pendingShortcutAction.value = Pair(shortcutAction, templateId)
        }
    }

    private fun createDynamicShortcuts(app: GymTrackerApp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return
        val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return

        val freeWorkoutIntent = Intent(this, MainActivity::class.java).apply {
            action = GymTrackerApp.ACTION_FREE_WORKOUT
        }
        val freeWorkoutShortcut = ShortcutInfo.Builder(this, "shortcut_free_workout")
            .setShortLabel("Séance libre")
            .setLongLabel("Démarrer une séance libre")
            .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher_round))
            .setIntent(freeWorkoutIntent)
            .build()

        val shortcuts = mutableListOf(freeWorkoutShortcut)

        // Raccourci vers le dernier template utilisé (chargé en arrière-plan)
        CoroutineScope(Dispatchers.IO).launch {
            val templates = app.repository.allTemplatesWithExercises.firstOrNull()
            val firstTemplate = templates?.firstOrNull()
            if (firstTemplate != null) {
                val templateIntent = Intent(this@MainActivity, MainActivity::class.java).apply {
                    action = GymTrackerApp.ACTION_START_TEMPLATE
                    putExtra(GymTrackerApp.EXTRA_TEMPLATE_ID, firstTemplate.template.id)
                }
                val templateShortcut = ShortcutInfo.Builder(this@MainActivity, "shortcut_template")
                    .setShortLabel(firstTemplate.template.name.take(12))
                    .setLongLabel("Démarrer : ${firstTemplate.template.name}")
                    .setIcon(Icon.createWithResource(this@MainActivity, R.mipmap.ic_launcher_round))
                    .setIntent(templateIntent)
                    .build()
                shortcutManager.dynamicShortcuts = listOf(freeWorkoutShortcut, templateShortcut)
            } else {
                shortcutManager.dynamicShortcuts = shortcuts
            }
        }
    }
}
