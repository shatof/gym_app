package com.gymtracker.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.gymtracker.app.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RestTimerService : Service() {

    companion object {
        const val CHANNEL_ID_TIMER = "rest_timer_running_channel"
        const val CHANNEL_ID_FINISHED = "rest_timer_finished_channel"
        const val NOTIFICATION_ID_TIMER = 1001
        const val NOTIFICATION_ID_FINISHED = 1002

        const val ACTION_START = "com.gymtracker.app.START_TIMER"
        const val ACTION_STOP = "com.gymtracker.app.STOP_TIMER"
        const val EXTRA_SECONDS = "extra_seconds"

        private val _remainingSeconds = MutableStateFlow<Int?>(null)
        val remainingSeconds: StateFlow<Int?> = _remainingSeconds.asStateFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        // Signal que le timer est terminé (pour fermer l'UI)
        private val _timerFinished = MutableStateFlow(false)
        val timerFinished: StateFlow<Boolean> = _timerFinished.asStateFlow()

        fun resetFinishedState() {
            _timerFinished.value = false
        }

        fun startTimer(context: Context, seconds: Int) {
            _timerFinished.value = false
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SECONDS, seconds)
            }
            context.startForegroundService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val binder = LocalBinder()
    private var countDownTimer: CountDownTimer? = null
    private var totalSeconds: Int = 0
    private var mediaPlayer: MediaPlayer? = null

    inner class LocalBinder : Binder() {
        fun getService(): RestTimerService = this@RestTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 180)
                startCountdown(seconds)
            }
            ACTION_STOP -> {
                stopCountdown()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Canal pour le timer en cours (silencieux)
        val timerChannel = NotificationChannel(
            CHANNEL_ID_TIMER,
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
            CHANNEL_ID_FINISHED,
            "Fin de repos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notification quand le repos est terminé"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        notificationManager.createNotificationChannel(finishedChannel)
    }

    private fun startCountdown(seconds: Int) {
        // Arrêter le timer précédent s'il existe
        countDownTimer?.cancel()

        totalSeconds = seconds
        _remainingSeconds.value = seconds
        _isRunning.value = true
        _timerFinished.value = false

        // Démarrer le service en premier plan
        startForeground(NOTIFICATION_ID_TIMER, createTimerNotification(seconds))

        countDownTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = (millisUntilFinished / 1000).toInt()
                _remainingSeconds.value = remaining
                updateNotification(remaining)
            }

            override fun onFinish() {
                _remainingSeconds.value = 0
                _isRunning.value = false
                onTimerFinished()
            }
        }.start()
    }

    private fun stopCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
        _remainingSeconds.value = null
        _isRunning.value = false

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID_TIMER)

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createTimerNotification(remainingSeconds: Int): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = "%02d:%02d".format(minutes, seconds)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_TIMER)
            .setContentTitle("⏱️ Repos en cours")
            .setContentText("Temps restant : $timeText")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Arrêter", stopPendingIntent)
            .setProgress(totalSeconds, totalSeconds - remainingSeconds, false)
            .build()
    }

    private fun updateNotification(remainingSeconds: Int) {
        val notification = createTimerNotification(remainingSeconds)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_TIMER, notification)
    }

    private fun onTimerFinished() {
        // Arrêter le service foreground et supprimer la notification du timer
        stopForeground(STOP_FOREGROUND_REMOVE)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID_TIMER)

        // Jouer le son d'alarme
        playAlarmSound()

        // Vibrer
        vibrate()

        // Envoyer la notification de fin
        sendFinishedNotification()

        // Signaler à l'UI que le timer est terminé
        _timerFinished.value = true
        _remainingSeconds.value = null

        // Arrêter le service
        stopSelf()
    }

    private fun playAlarmSound() {
        try {
            // Utiliser MediaPlayer pour un son plus fort et fiable
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(applicationContext, alarmUri)
                prepare()
                start()

                // Arrêter après 2 secondes
                setOnCompletionListener {
                    it.release()
                }
            }

            // Arrêter le son après 2 secondes si ce n'est pas fini
            android.os.Handler(mainLooper).postDelayed({
                try {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                } catch (e: Exception) {
                    // Ignore
                }
            }, 2000)

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: utiliser Ringtone
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
                ringtone?.play()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun vibrate() {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VibratorManager::class.java)
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            // Vibration plus longue et distinctive
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendFinishedNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_FINISHED)
            .setContentTitle("💪 Repos terminé !")
            .setContentText("C'est le moment de reprendre")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_FINISHED, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        _remainingSeconds.value = null
        _isRunning.value = false
    }
}
