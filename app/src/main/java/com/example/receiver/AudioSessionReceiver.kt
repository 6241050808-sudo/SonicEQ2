package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log
import com.example.SonicEqApplication
import com.example.service.AudioEqualizerService

class AudioSessionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AudioSessionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return

        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "Streaming App"

        Log.d(TAG, "Audio session event: $action, sessionId: $sessionId, app: $packageName")

        if (sessionId == AudioEffect.ERROR) return

        try {
            val app = SonicEqApplication.instance
            when (action) {
                AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                    app.audioEngine.registerSession(sessionId, packageName)
                    // Start foreground service to keep effects running smoothly
                    AudioEqualizerService.startService(context)
                }
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                    app.audioEngine.unregisterSession(sessionId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling audio session broadcast: ${e.message}")
        }
    }
}
