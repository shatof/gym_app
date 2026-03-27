package com.gymtracker.app

import android.app.Application
import com.gymtracker.app.data.GymDatabase
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow

class GymTrackerApp : Application() {

    val database by lazy { GymDatabase.getDatabase(this) }

    val repository by lazy {
        GymRepository(
            database.workoutDao(),
            database.exerciseDao(),
            database.exerciseSetDao(),
            database.sessionTemplateDao(),
            database.templateExerciseDao(),
            database.measurementDao()
        )
    }

    /**
     * Action déclenchée par un raccourci Android (long-press icône).
     * Pair(action: String, templateId: Long?)
     */
    val pendingShortcutAction = MutableStateFlow<Pair<String, Long?>?>(null)

    companion object {
        const val ACTION_FREE_WORKOUT   = "com.gymtracker.app.ACTION_FREE_WORKOUT"
        const val ACTION_START_TEMPLATE = "com.gymtracker.app.ACTION_START_TEMPLATE"
        const val EXTRA_TEMPLATE_ID     = "template_id"
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
