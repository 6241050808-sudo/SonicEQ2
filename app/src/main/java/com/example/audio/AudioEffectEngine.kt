package com.example.audio

import android.content.Context
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log
import com.example.data.EqPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class AudioEffectEngine(private val context: Context) {

    companion object {
        private const val TAG = "AudioEffectEngine"
        val DEFAULT_FREQUENCIES = listOf(60_000, 230_000, 910_000, 3_600_000, 14_000_000)
    }

    private val prefs = EqPreferences(context)

    // Active session IDs mapped to their audiofx instances
    private data class SessionFx(
        val equalizer: Equalizer?,
        val bassBoost: BassBoost?,
        val virtualizer: Virtualizer?,
        val loudnessEnhancer: LoudnessEnhancer?
    )

    private val activeSessions = ConcurrentHashMap<Int, SessionFx>()

    // Current State Flow for UI
    private val _isEqEnabled = MutableStateFlow(prefs.isEqEnabled)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _bandLevels = MutableStateFlow<List<Int>>(
        List(5) { i -> prefs.getBandLevel(i, 0) }
    )
    val bandLevels: StateFlow<List<Int>> = _bandLevels.asStateFlow()

    private val _bassBoost = MutableStateFlow(prefs.bassBoost)
    val bassBoost: StateFlow<Int> = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow(prefs.virtualizer)
    val virtualizer: StateFlow<Int> = _virtualizer.asStateFlow()

    private val _loudness = MutableStateFlow(prefs.loudness)
    val loudness: StateFlow<Int> = _loudness.asStateFlow()

    private val _activeSessionCount = MutableStateFlow(0)
    val activeSessionCount: StateFlow<Int> = _activeSessionCount.asStateFlow()

    private val _lastActivePackage = MutableStateFlow<String?>(null)
    val lastActivePackage: StateFlow<String?> = _lastActivePackage.asStateFlow()

    var minBandLevel: Short = -1500
    var maxBandLevel: Short = 1500
    var numberOfBands: Short = 5
    var bandFrequencies: List<Int> = DEFAULT_FREQUENCIES

    init {
        // Attempt initializing with global session (0) or probing hardware capabilities
        probeHardwareCapabilities()
    }

    private fun probeHardwareCapabilities() {
        try {
            val probeEq = Equalizer(0, 0)
            numberOfBands = probeEq.numberOfBands
            val range = probeEq.bandLevelRange
            if (range != null && range.size >= 2) {
                minBandLevel = range[0]
                maxBandLevel = range[1]
            }
            val freqs = mutableListOf<Int>()
            for (i in 0 until numberOfBands) {
                freqs.add(probeEq.getCenterFreq(i.toShort()))
            }
            if (freqs.isNotEmpty()) {
                bandFrequencies = freqs
            }
            probeEq.release()
        } catch (e: Exception) {
            Log.w(TAG, "Hardware EQ probe session 0 error (normal on some Android versions): ${e.message}")
            // Fallback to 5 standard bands
            numberOfBands = 5
            minBandLevel = -1500
            maxBandLevel = 1500
            bandFrequencies = DEFAULT_FREQUENCIES
        }

        // Adjust bandLevels state size if needed
        val currentLevels = _bandLevels.value.toMutableList()
        while (currentLevels.size < numberOfBands.toInt()) {
            val idx = currentLevels.size
            currentLevels.add(prefs.getBandLevel(idx, 0))
        }
        _bandLevels.value = currentLevels.take(numberOfBands.toInt())

        // Also register session 0 if possible
        registerSession(0, "System Audio")
    }

    @Synchronized
    fun registerSession(sessionId: Int, packageName: String? = null) {
        if (activeSessions.containsKey(sessionId)) {
            Log.d(TAG, "Session $sessionId already registered")
            return
        }

        try {
            val eq = try {
                Equalizer(1000, sessionId).apply {
                    enabled = _isEqEnabled.value
                    // Apply current band levels
                    val levels = _bandLevels.value
                    for (i in 0 until numberOfBands.toInt()) {
                        if (i < levels.size) {
                            val clamped = levels[i].coerceIn(minBandLevel.toInt(), maxBandLevel.toInt()).toShort()
                            setBandLevel(i.toShort(), clamped)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed creating Equalizer for session $sessionId: ${e.message}")
                null
            }

            val bb = try {
                BassBoost(1000, sessionId).apply {
                    if (strengthSupported) {
                        setStrength(_bassBoost.value.toShort())
                        enabled = _isEqEnabled.value && _bassBoost.value > 0
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed creating BassBoost for session $sessionId: ${e.message}")
                null
            }

            val virt = try {
                Virtualizer(1000, sessionId).apply {
                    if (strengthSupported) {
                        setStrength(_virtualizer.value.toShort())
                        enabled = _isEqEnabled.value && _virtualizer.value > 0
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed creating Virtualizer for session $sessionId: ${e.message}")
                null
            }

            val loudnessEnhancer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    LoudnessEnhancer(sessionId).apply {
                        setTargetGain(_loudness.value)
                        enabled = _isEqEnabled.value && _loudness.value > 0
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed creating LoudnessEnhancer for session $sessionId: ${e.message}")
                    null
                }
            } else null

            activeSessions[sessionId] = SessionFx(eq, bb, virt, loudnessEnhancer)
            _activeSessionCount.value = activeSessions.size
            if (packageName != null && packageName != "System Audio") {
                _lastActivePackage.value = packageName
            }
            Log.i(TAG, "Successfully registered audiofx for session $sessionId (App: $packageName)")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering session $sessionId: ${e.message}")
        }
    }

    @Synchronized
    fun unregisterSession(sessionId: Int) {
        val fx = activeSessions.remove(sessionId)
        if (fx != null) {
            try { fx.equalizer?.release() } catch (_: Exception) {}
            try { fx.bassBoost?.release() } catch (_: Exception) {}
            try { fx.virtualizer?.release() } catch (_: Exception) {}
            try { fx.loudnessEnhancer?.release() } catch (_: Exception) {}
            _activeSessionCount.value = activeSessions.size
            Log.i(TAG, "Unregistered session $sessionId")
        }
    }

    fun setEqEnabled(enabled: Boolean) {
        _isEqEnabled.value = enabled
        prefs.isEqEnabled = enabled
        activeSessions.values.forEach { fx ->
            try { fx.equalizer?.enabled = enabled } catch (_: Exception) {}
            try { fx.bassBoost?.enabled = enabled && _bassBoost.value > 0 } catch (_: Exception) {}
            try { fx.virtualizer?.enabled = enabled && _virtualizer.value > 0 } catch (_: Exception) {}
            try { fx.loudnessEnhancer?.enabled = enabled && _loudness.value > 0 } catch (_: Exception) {}
        }
    }

    fun setBandLevel(bandIndex: Int, levelInMb: Int) {
        val currentLevels = _bandLevels.value.toMutableList()
        if (bandIndex in currentLevels.indices) {
            currentLevels[bandIndex] = levelInMb
            _bandLevels.value = currentLevels
            prefs.setBandLevel(bandIndex, levelInMb)

            val clamped = levelInMb.coerceIn(minBandLevel.toInt(), maxBandLevel.toInt()).toShort()
            activeSessions.values.forEach { fx ->
                try {
                    fx.equalizer?.setBandLevel(bandIndex.toShort(), clamped)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to set band level on session EQ: ${e.message}")
                }
            }
        }
    }

    fun setAllBandLevels(levels: List<Int>) {
        val clampedLevels = levels.mapIndexed { index, level ->
            val clamped = level.coerceIn(minBandLevel.toInt(), maxBandLevel.toInt())
            prefs.setBandLevel(index, clamped)
            clamped
        }
        _bandLevels.value = clampedLevels

        activeSessions.values.forEach { fx ->
            val eq = fx.equalizer ?: return@forEach
            try {
                clampedLevels.forEachIndexed { index, level ->
                    if (index < numberOfBands) {
                        eq.setBandLevel(index.toShort(), level.toShort())
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to apply preset band levels: ${e.message}")
            }
        }
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _bassBoost.value = clamped
        prefs.bassBoost = clamped

        activeSessions.values.forEach { fx ->
            try {
                fx.bassBoost?.let { bb ->
                    if (bb.strengthSupported) {
                        bb.setStrength(clamped.toShort())
                        bb.enabled = _isEqEnabled.value && clamped > 0
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error setting bass boost: ${e.message}")
            }
        }
    }

    fun setVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _virtualizer.value = clamped
        prefs.virtualizer = clamped

        activeSessions.values.forEach { fx ->
            try {
                fx.virtualizer?.let { virt ->
                    if (virt.strengthSupported) {
                        virt.setStrength(clamped.toShort())
                        virt.enabled = _isEqEnabled.value && clamped > 0
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error setting virtualizer: ${e.message}")
            }
        }
    }

    fun setLoudness(gainMb: Int) {
        val clamped = gainMb.coerceIn(0, 1000)
        _loudness.value = clamped
        prefs.loudness = clamped

        activeSessions.values.forEach { fx ->
            try {
                fx.loudnessEnhancer?.let { le ->
                    le.setTargetGain(clamped)
                    le.enabled = _isEqEnabled.value && clamped > 0
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error setting loudness: ${e.message}")
            }
        }
    }

    fun applyPreset(presetBandLevels: List<Int>, bbStrength: Int, virtStrength: Int, loudGain: Int) {
        setAllBandLevels(presetBandLevels)
        setBassBoost(bbStrength)
        setVirtualizer(virtStrength)
        setLoudness(loudGain)
    }

    fun releaseAll() {
        activeSessions.keys.toList().forEach { unregisterSession(it) }
    }
}
