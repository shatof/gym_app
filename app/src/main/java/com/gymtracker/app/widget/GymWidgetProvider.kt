package com.gymtracker.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.gymtracker.app.GymTrackerApp
import com.gymtracker.app.MainActivity
import com.gymtracker.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class GymWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as GymTrackerApp
        CoroutineScope(Dispatchers.IO).launch {
            val stats = app.repository.getUserStats()
            val weeklyStats = app.repository.getWeeklyStats()
            val templates = app.repository.allTemplatesWithExercises.first()
            val firstTemplateId = templates.firstOrNull()?.template?.id

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.gym_widget)

                // Streak
                views.setTextViewText(R.id.widget_streak, "🔥 ${stats.currentStreak}")

                // Sessions cette semaine
                views.setTextViewText(R.id.widget_sessions, "💪 ${weeklyStats.sessionsCount}")

                // Bouton séance libre
                val freeIntent = Intent(context, MainActivity::class.java).apply {
                    action = GymTrackerApp.ACTION_FREE_WORKOUT
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val freePi = PendingIntent.getActivity(
                    context, 0, freeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_start, freePi)

                // Bouton template
                if (firstTemplateId != null) {
                    val templateIntent = Intent(context, MainActivity::class.java).apply {
                        action = GymTrackerApp.ACTION_START_TEMPLATE
                        putExtra(GymTrackerApp.EXTRA_TEMPLATE_ID, firstTemplateId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val templatePi = PendingIntent.getActivity(
                        context, 1, templateIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_btn_template, templatePi)
                    views.setTextViewText(
                        R.id.widget_btn_template,
                        "📋 ${templates.first().template.name.take(10)}"
                    )
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}
