package com.gymtracker.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gymtracker.app.data.dao.ExerciseDao
import com.gymtracker.app.data.dao.ExerciseSetDao
import com.gymtracker.app.data.dao.SessionTemplateDao
import com.gymtracker.app.data.dao.TemplateExerciseDao
import com.gymtracker.app.data.dao.WorkoutDao
import com.gymtracker.app.data.model.Exercise
import com.gymtracker.app.data.model.ExerciseSet
import com.gymtracker.app.data.model.SessionTemplate
import com.gymtracker.app.data.model.TemplateExercise
import com.gymtracker.app.data.model.Workout

@Database(
    entities = [
        Workout::class,
        Exercise::class,
        ExerciseSet::class,
        SessionTemplate::class,
        TemplateExercise::class
    ],
    version = 4,
    exportSchema = true
)
abstract class GymDatabase : RoomDatabase() {
    
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun sessionTemplateDao(): SessionTemplateDao
    abstract fun templateExerciseDao(): TemplateExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null
        
        fun getDatabase(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
