package com.gymtracker.app

import android.app.Application
import com.gymtracker.app.data.GymDatabase
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.util.NotificationHelper

class GymTrackerApp : Application() {
    
    val database by lazy { GymDatabase.getDatabase(this) }
    
    val repository by lazy {
        GymRepository(
            database.workoutDao(),
            database.exerciseDao(),
            database.exerciseSetDao(),
            database.sessionTemplateDao(),
            database.templateExerciseDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
