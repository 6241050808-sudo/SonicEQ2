package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.audio.AudioEffectEngine
import com.example.audio.BluetoothDeviceManager
import com.example.data.AppDatabase
import com.example.data.EqPreferences

class SonicEqApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var audioEngine: AudioEffectEngine
        private set

    lateinit var bluetoothManager: BluetoothDeviceManager
        private set

    lateinit var preferences: EqPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        preferences = EqPreferences(this)
        audioEngine = AudioEffectEngine(this)
        bluetoothManager = BluetoothDeviceManager(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_EQUALIZER,
                "SonicEQ Active Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active audio equalizer status and Bluetooth connection"
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ID_BLUETOOTH,
                "Bluetooth Device Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a saved Bluetooth audio device connects"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_EQUALIZER = "channel_soniceq_service"
        const val CHANNEL_ID_BLUETOOTH = "channel_soniceq_bt"

        lateinit var instance: SonicEqApplication
            private set
    }
}
