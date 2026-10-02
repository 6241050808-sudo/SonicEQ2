package com.example.data

import android.content.Context
import android.content.SharedPreferences

class EqPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("soniceq_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EQ_ENABLED = "eq_enabled"
        private const val KEY_BASS_BOOST = "bass_boost"
        private const val KEY_VIRTUALIZER = "virtualizer"
        private const val KEY_LOUDNESS = "loudness"
        private const val KEY_ACTIVE_PRESET_ID = "active_preset_id"
        private const val KEY_BAND_LEVELS_PREFIX = "band_level_"
        private const val KEY_LAST_BT_DEVICE = "last_bt_device"
        private const val KEY_AUTO_BLUETOOTH_ENABLED = "auto_bluetooth_enabled"
    }

    var isEqEnabled: Boolean
        get() = prefs.getBoolean(KEY_EQ_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_EQ_ENABLED, value).apply()

    var bassBoost: Int
        get() = prefs.getInt(KEY_BASS_BOOST, 300)
        set(value) = prefs.edit().putInt(KEY_BASS_BOOST, value).apply()

    var virtualizer: Int
        get() = prefs.getInt(KEY_VIRTUALIZER, 200)
        set(value) = prefs.edit().putInt(KEY_VIRTUALIZER, value).apply()

    var loudness: Int
        get() = prefs.getInt(KEY_LOUDNESS, 100)
        set(value) = prefs.edit().putInt(KEY_LOUDNESS, value).apply()

    var activePresetId: Long
        get() = prefs.getLong(KEY_ACTIVE_PRESET_ID, 1L)
        set(value) = prefs.edit().putLong(KEY_ACTIVE_PRESET_ID, value).apply()

    var lastConnectedDeviceName: String
        get() = prefs.getString(KEY_LAST_BT_DEVICE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_BT_DEVICE, value).apply()

    var isAutoBluetoothEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_BLUETOOTH_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_BLUETOOTH_ENABLED, value).apply()

    fun getBandLevel(band: Int, defaultLevel: Int = 0): Int {
        return prefs.getInt("$KEY_BAND_LEVELS_PREFIX$band", defaultLevel)
    }

    fun setBandLevel(band: Int, level: Int) {
        prefs.edit().putInt("$KEY_BAND_LEVELS_PREFIX$band", level).apply()
    }
}
