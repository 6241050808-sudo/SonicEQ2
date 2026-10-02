package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SonicEqApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AudioEqualizerService : Service() {

    companion object {
        const val ACTION_START = "com.example.soniceq.START"
        const val ACTION_TOGGLE_EQ = "com.example.soniceq.TOGGLE_EQ"
        const val ACTION_STOP = "com.example.soniceq.STOP"
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, AudioEqualizerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var observerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = SonicEqApplication.instance
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_EQ -> {
                val current = app.audioEngine.isEqEnabled.value
                app.audioEngine.setEqEnabled(!current)
                updateNotification()
            }
            ACTION_START -> {
                startForegroundWithNotification()
            }
            else -> {
                startForegroundWithNotification()
            }
        }

        observeStateChanges()
        return START_STICKY
    }

    private fun observeStateChanges() {
        if (observerJob != null) return
        observerJob = serviceScope.launch {
            val app = SonicEqApplication.instance
            launch {
                app.audioEngine.isEqEnabled.collectLatest {
                    updateNotification()
                }
            }
            launch {
                app.bluetoothManager.connectedDeviceName.collectLatest {
                    updateNotification()
                }
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val app = SonicEqApplication.instance
        val isEqOn = app.audioEngine.isEqEnabled.value
        val btDevice = app.bluetoothManager.connectedDeviceName.value

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, AudioEqualizerService::class.java).apply {
            action = ACTION_TOGGLE_EQ
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AudioEqualizerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isEqOn) "Equalizer Active" else "Equalizer Bypassed"
        val subtitle = if (!btDevice.isNullOrBlank()) {
            "🎧 $btDevice • $statusText"
        } else {
            statusText
        }

        val toggleLabel = if (isEqOn) "Disable EQ" else "Enable EQ"

        return NotificationCompat.Builder(this, SonicEqApplication.CHANNEL_ID_EQUALIZER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("SonicEQ Studio")
            .setContentText(subtitle)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, toggleLabel, togglePendingIntent)
            .addAction(0, "Close", stopPendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        observerJob?.cancel()
    }
}
