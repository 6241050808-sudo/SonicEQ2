package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SonicEqApplication
import com.example.audio.DiscoveredBluetoothDevice
import com.example.audio.ToneAudioPlayer
import com.example.model.BluetoothDeviceConfig
import com.example.model.EqPreset
import com.example.model.StreamingApp
import com.example.service.AudioEqualizerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EqualizerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SonicEqApplication
    private val audioEngine = app.audioEngine
    private val btManager = app.bluetoothManager
    private val database = app.database
    private val prefs = app.preferences

    val isEqEnabled: StateFlow<Boolean> = audioEngine.isEqEnabled
    val bandLevels: StateFlow<List<Int>> = audioEngine.bandLevels
    val bassBoost: StateFlow<Int> = audioEngine.bassBoost
    val virtualizer: StateFlow<Int> = audioEngine.virtualizer
    val loudness: StateFlow<Int> = audioEngine.loudness
    val activeSessionCount: StateFlow<Int> = audioEngine.activeSessionCount
    val lastActivePackage: StateFlow<String?> = audioEngine.lastActivePackage

    val bandFrequencies: List<Int> get() = audioEngine.bandFrequencies
    val minBandLevel: Short get() = audioEngine.minBandLevel
    val maxBandLevel: Short get() = audioEngine.maxBandLevel

    val connectedBluetoothDevice: StateFlow<String?> = btManager.connectedDeviceName

    val presets: StateFlow<List<EqPreset>> = database.presetDao().getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedBluetoothDevices: StateFlow<List<BluetoothDeviceConfig>> = database.bluetoothDeviceDao().getAllDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activePreset = MutableStateFlow<EqPreset?>(null)
    val activePreset: StateFlow<EqPreset?> = _activePreset.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<DiscoveredBluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _streamingApps = MutableStateFlow<List<StreamingApp>>(emptyList())
    val streamingApps: StateFlow<List<StreamingApp>> = _streamingApps.asStateFlow()

    private val tonePlayer = ToneAudioPlayer { testSessionId ->
        audioEngine.registerSession(testSessionId, "SonicEQ Built-in Tester")
    }
    val isTestAudioPlaying: StateFlow<Boolean> = tonePlayer.isPlaying

    init {
        // Start foreground service if enabled
        if (prefs.isEqEnabled) {
            AudioEqualizerService.startService(application)
        }

        refreshPairedDevices()
        refreshStreamingApps()

        viewModelScope.launch {
            presets.collect { list ->
                if (list.isNotEmpty() && _activePreset.value == null) {
                    val savedId = prefs.activePresetId
                    val found = list.find { it.id == savedId } ?: list.first()
                    _activePreset.value = found
                }
            }
        }
    }

    fun toggleEq(enabled: Boolean) {
        audioEngine.setEqEnabled(enabled)
        if (enabled) {
            AudioEqualizerService.startService(getApplication())
        }
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        audioEngine.setBandLevel(bandIndex, level)
        // Mark current preset as customized
        _activePreset.value?.let { current ->
            if (current.isDefault) {
                _activePreset.value = current.copy(name = "${current.name} (Custom)")
            }
        }
    }

    fun setBassBoost(value: Int) {
        audioEngine.setBassBoost(value)
    }

    fun setVirtualizer(value: Int) {
        audioEngine.setVirtualizer(value)
    }

    fun setLoudness(value: Int) {
        audioEngine.setLoudness(value)
    }

    fun selectPreset(preset: EqPreset) {
        _activePreset.value = preset
        prefs.activePresetId = preset.id
        audioEngine.applyPreset(
            presetBandLevels = preset.bandLevels,
            bbStrength = preset.bassBoost,
            virtStrength = preset.virtualizer,
            loudGain = preset.loudnessEnhancer
        )
    }

    fun saveCurrentAsNewPreset(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newPreset = EqPreset(
                name = name.trim(),
                bandLevels = bandLevels.value,
                bassBoost = bassBoost.value,
                virtualizer = virtualizer.value,
                loudnessEnhancer = loudness.value,
                isDefault = false,
                isCustom = true
            )
            val insertedId = database.presetDao().insertPreset(newPreset)
            _activePreset.value = newPreset.copy(id = insertedId)
            prefs.activePresetId = insertedId
        }
    }

    fun deletePreset(preset: EqPreset) {
        if (preset.isDefault) return
        viewModelScope.launch {
            database.presetDao().deletePreset(preset)
            if (_activePreset.value?.id == preset.id) {
                val fallback = presets.value.firstOrNull { it.isDefault }
                if (fallback != null) selectPreset(fallback)
            }
        }
    }

    fun saveBluetoothDevice(
        key: String,
        name: String,
        assignedPresetId: Long,
        autoStartApp: Boolean,
        autoEnableEq: Boolean
    ) {
        viewModelScope.launch {
            val config = BluetoothDeviceConfig(
                deviceKey = key.ifBlank { name },
                deviceName = name.trim(),
                assignedPresetId = assignedPresetId,
                autoStartApp = autoStartApp,
                autoEnableEq = autoEnableEq,
                lastConnectedTimestamp = System.currentTimeMillis()
            )
            database.bluetoothDeviceDao().saveDevice(config)
            refreshPairedDevices()
        }
    }

    fun deleteBluetoothDevice(config: BluetoothDeviceConfig) {
        viewModelScope.launch {
            database.bluetoothDeviceDao().deleteDevice(config)
            refreshPairedDevices()
        }
    }

    fun refreshPairedDevices() {
        _pairedDevices.value = btManager.getPairedDevices()
    }

    fun refreshStreamingApps() {
        _streamingApps.value = StreamingApp.getSupportedApps(getApplication())
    }

    fun toggleTestAudio() {
        tonePlayer.togglePlay()
    }

    fun resetToFlat() {
        val flatLevels = List(bandLevels.value.size) { 0 }
        audioEngine.applyPreset(flatLevels, 0, 0, 0)
        _activePreset.value = presets.value.find { it.name.startsWith("Flat") }
    }

    override fun onCleared() {
        super.onCleared()
        tonePlayer.stop()
    }
}
