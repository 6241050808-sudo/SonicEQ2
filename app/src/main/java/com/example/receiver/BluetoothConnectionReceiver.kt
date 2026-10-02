package com.example.receiver

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SonicEqApplication
import com.example.model.BluetoothDeviceConfig
import com.example.service.AudioEqualizerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothConnectionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BluetoothReceiver"
        private const val NOTIF_ID_BT_EVENT = 2002
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return

        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        val deviceName = try {
            device?.name ?: intent.getStringExtra("android.bluetooth.device.extra.NAME") ?: "Bluetooth Device"
        } catch (_: Exception) {
            "Bluetooth Device"
        }
        val deviceAddress = device?.address ?: ""

        Log.d(TAG, "Bluetooth event: $action for device: $deviceName ($deviceAddress)")

        val app = SonicEqApplication.instance

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                app.bluetoothManager.setConnectedDevice(deviceName)
                handleDeviceConnected(context, deviceName, deviceAddress)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                app.bluetoothManager.setConnectedDevice(null)
            }
            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" -> {
                val state = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1)
                if (state == 2) { // STATE_CONNECTED
                    app.bluetoothManager.setConnectedDevice(deviceName)
                    handleDeviceConnected(context, deviceName, deviceAddress)
                } else if (state == 0) { // STATE_DISCONNECTED
                    app.bluetoothManager.setConnectedDevice(null)
                }
            }
        }
    }

    private fun handleDeviceConnected(context: Context, deviceName: String, deviceAddress: String) {
        val app = SonicEqApplication.instance
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = app.database.bluetoothDeviceDao()
                val config = dao.findDevice(deviceAddress, deviceName)

                if (config != null) {
                    // Update last connected timestamp
                    dao.updateDevice(config.copy(lastConnectedTimestamp = System.currentTimeMillis()))

                    // Check if auto-enable EQ is set
                    if (config.autoEnableEq) {
                        app.audioEngine.setEqEnabled(true)
                    }

                    // Apply assigned preset
                    val presetDao = app.database.presetDao()
                    val preset = presetDao.getPresetById(config.assignedPresetId)
                    var presetName = "Default"
                    if (preset != null) {
                        presetName = preset.name
                        app.audioEngine.applyPreset(
                            presetBandLevels = preset.bandLevels,
                            bbStrength = preset.bassBoost,
                            virtStrength = preset.virtualizer,
                            loudGain = preset.loudnessEnhancer
                        )
                        app.preferences.activePresetId = preset.id
                    }

                    // Always ensure service is running for background EQ
                    AudioEqualizerService.startService(context)

                    // Notify or launch app if configured
                    showConnectedNotification(context, config, presetName)

                    if (config.autoStartApp) {
                        try {
                            val launchIntent = Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("EXTRA_CONNECTED_DEVICE", config.deviceName)
                            }
                            context.startActivity(launchIntent)
                        } catch (e: Exception) {
                            Log.w(TAG, "Cannot launch activity directly from background, notification was shown instead: ${e.message}")
                        }
                    }
                } else {
                    // Device connected but not specifically saved with custom profile
                    // Still ensure service is active if EQ is enabled in preferences
                    if (app.preferences.isEqEnabled) {
                        AudioEqualizerService.startService(context)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing Bluetooth connection: ${e.message}")
            }
        }
    }

    private fun showConnectedNotification(context: Context, config: BluetoothDeviceConfig, presetName: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_CONNECTED_DEVICE", config.deviceName)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            201,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SonicEqApplication.CHANNEL_ID_BLUETOOTH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Connected: ${config.deviceName}")
            .setContentText("Applied $presetName profile • EQ is Active")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID_BT_EVENT, notification)
    }
}
