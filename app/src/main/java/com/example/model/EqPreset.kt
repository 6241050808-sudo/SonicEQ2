package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eq_presets")
data class EqPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    // Millibels (-1500 mB to +1500 mB, representing -15dB to +15dB) for each band
    val bandLevels: List<Int>,
    val bassBoost: Int = 0, // 0 to 1000
    val virtualizer: Int = 0, // 0 to 1000
    val loudnessEnhancer: Int = 0, // 0 to 1000 mB
    val isDefault: Boolean = false,
    val isCustom: Boolean = false
) {
    companion object {
        fun defaultPresets(): List<EqPreset> = listOf(
            EqPreset(
                id = 1,
                name = "Flat / Studio",
                bandLevels = listOf(0, 0, 0, 0, 0),
                bassBoost = 0,
                virtualizer = 0,
                loudnessEnhancer = 0,
                isDefault = true
            ),
            EqPreset(
                id = 2,
                name = "Bass Booster",
                bandLevels = listOf(600, 450, 0, 150, 200),
                bassBoost = 750,
                virtualizer = 200,
                loudnessEnhancer = 200,
                isDefault = true
            ),
            EqPreset(
                id = 3,
                name = "Rock",
                bandLevels = listOf(450, 250, -150, 300, 500),
                bassBoost = 400,
                virtualizer = 350,
                loudnessEnhancer = 150,
                isDefault = true
            ),
            EqPreset(
                id = 4,
                name = "Pop & Vocal",
                bandLevels = listOf(-100, 150, 450, 250, -50),
                bassBoost = 250,
                virtualizer = 300,
                loudnessEnhancer = 100,
                isDefault = true
            ),
            EqPreset(
                id = 5,
                name = "Electronic / EDM",
                bandLevels = listOf(550, 300, 0, 200, 450),
                bassBoost = 650,
                virtualizer = 500,
                loudnessEnhancer = 250,
                isDefault = true
            ),
            EqPreset(
                id = 6,
                name = "Jazz & Acoustic",
                bandLevels = listOf(250, 100, 200, 250, 150),
                bassBoost = 150,
                virtualizer = 400,
                loudnessEnhancer = 50,
                isDefault = true
            ),
            EqPreset(
                id = 7,
                name = "Hip Hop",
                bandLevels = listOf(600, 350, -100, 150, 300),
                bassBoost = 700,
                virtualizer = 250,
                loudnessEnhancer = 300,
                isDefault = true
            ),
            EqPreset(
                id = 8,
                name = "Classical",
                bandLevels = listOf(350, 250, 200, 150, -100),
                bassBoost = 100,
                virtualizer = 450,
                loudnessEnhancer = 50,
                isDefault = true
            ),
            EqPreset(
                id = 9,
                name = "Car Stereo Punch",
                bandLevels = listOf(700, 400, 50, 200, 400),
                bassBoost = 800,
                virtualizer = 300,
                loudnessEnhancer = 400,
                isDefault = true
            )
        )
    }
}
