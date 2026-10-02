package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class ToneAudioPlayer(private val onSessionCreated: (Int) -> Unit) {

    companion object {
        private const val TAG = "ToneAudioPlayer"
        private const val SAMPLE_RATE = 44100
    }

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun togglePlay() {
        if (_isPlaying.value) {
            stop()
        } else {
            start()
        }
    }

    fun start() {
        if (_isPlaying.value) return

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufferSize * 2).coerceAtLeast(SAMPLE_RATE / 2)

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            audioTrack = track
            val sessionId = track.audioSessionId
            onSessionCreated(sessionId)

            track.play()
            _isPlaying.value = true

            playbackJob = scope.launch {
                val chunkSize = 2048
                val buffer = ShortArray(chunkSize)
                var sampleIndex = 0L

                while (isActive && _isPlaying.value) {
                    val beatPos = (sampleIndex % (SAMPLE_RATE)) / SAMPLE_RATE.toDouble()

                    for (i in 0 until chunkSize) {
                        val t = (sampleIndex + i).toDouble() / SAMPLE_RATE

                        // Synthesize 3 frequency bands to showcase EQ:
                        // 1. Deep Bass / Kick (70Hz - 90Hz)
                        val bassKick = sin(2 * PI * 80 * t) * (if (beatPos < 0.25) 0.6 else 0.2)
                        // 2. Midrange Chord (330Hz E4, 440Hz A4)
                        val midSynth = (sin(2 * PI * 330 * t) + sin(2 * PI * 440 * t)) * 0.18
                        // 3. Crisp Hi-Hat / High frequencies (5000Hz - 10000Hz bursts)
                        val highHat = if ((beatPos in 0.45..0.5) || (beatPos in 0.95..1.0)) {
                            (sin(2 * PI * 7500 * t) + sin(2 * PI * 11000 * t)) * 0.25
                        } else {
                            0.0
                        }

                        val sample = (bassKick + midSynth + highHat) * Short.MAX_VALUE * 0.65
                        buffer[i] = sample.coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
                    }

                    sampleIndex += chunkSize
                    try {
                        track.write(buffer, 0, chunkSize)
                    } catch (e: Exception) {
                        Log.e(TAG, "Write error: ${e.message}")
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting test audio: ${e.message}")
            _isPlaying.value = false
        }
    }

    fun stop() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping audio: ${e.message}")
        }
        audioTrack = null
    }
}
