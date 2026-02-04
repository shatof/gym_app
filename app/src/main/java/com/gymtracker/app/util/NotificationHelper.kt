package com.gymtracker.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager

/**
 * Helper pour créer les canaux de notification.
 * Note: Les notifications du timer sont gérées par RestTimerService.
 */
object NotificationHelper {

    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal pour le timer en cours (silencieux, géré par le service)
        val timerChannel = NotificationChannel(
            "rest_timer_running_channel",
            "Timer de repos",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Affiche le compte à rebours du repos"
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(timerChannel)

        // Canal pour la fin du timer (avec son)
        val finishedChannel = NotificationChannel(
            "rest_timer_finished_channel",
            "Fin de repos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notification quand le repos est terminé"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        notificationManager.createNotificationChannel(finishedChannel)
    }
}
