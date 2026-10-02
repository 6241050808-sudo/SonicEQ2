package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqPreset
import com.example.ui.components.EffectKnobCard
import com.example.ui.components.PresetChipRow
import com.example.ui.components.SpectrumVisualizer
import com.example.ui.components.VerticalEqSlider
import com.example.ui.theme.SonicAmber
import com.example.ui.theme.SonicBackground
import com.example.ui.theme.SonicCyan
import com.example.ui.theme.SonicGreen
import com.example.ui.theme.SonicPink
import com.example.ui.theme.SonicPurple
import com.example.ui.theme.SonicSurface
import com.example.ui.theme.SonicSurfaceBorder
import com.example.ui.theme.SonicSurfaceVariant
import com.example.ui.theme.SonicTextMuted
import com.example.ui.theme.SonicTextPrimary
import com.example.ui.theme.SonicTextSecondary
import com.example.viewmodel.EqualizerViewModel

@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    onNavigateToBluetooth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEqEnabled by viewModel.isEqEnabled.collectAsState()
    val bandLevels by viewModel.bandLevels.collectAsState()
    val bassBoost by viewModel.bassBoost.collectAsState()
    val virtualizer by viewModel.virtualizer.collectAsState()
    val loudness by viewModel.loudness.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val activePreset by viewModel.activePreset.collectAsState()
    val isTestAudioPlaying by viewModel.isTestAudioPlaying.collectAsState()
    val connectedDevice by viewModel.connectedBluetoothDevice.collectAsState()
    val activeSessionCount by viewModel.activeSessionCount.collectAsState()
    val lastActivePackage by viewModel.lastActivePackage.collectAsState()

    var showNewPresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SonicBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Master Control Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SonicSurface)
                .border(1.dp, if (isEqEnabled) SonicCyan.copy(alpha = 0.5f) else SonicSurfaceBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SonicEQ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = SonicTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isEqEnabled) SonicGreen.copy(alpha = 0.2f) else Color(0xFF334155))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isEqEnabled) "ACTIVE" else "BYPASSED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEqEnabled) SonicGreen else SonicTextMuted
                                )
                            }
                        }

                        val sessionStatus = when {
                            lastActivePackage != null -> "Streaming: ${lastActivePackage?.substringAfterLast('.')}"
                            activeSessionCount > 0 -> "$activeSessionCount active music stream(s)"
                            else -> "Ready for Spotify, YT Music & Bluetooth"
                        }
                        Text(
                            text = sessionStatus,
                            fontSize = 12.sp,
                            color = SonicTextSecondary
                        )
                    }

                    // Master Power Toggle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEqEnabled) SonicCyan.copy(alpha = 0.2f) else SonicSurfaceVariant
                            )
                            .border(
                                width = 2.dp,
                                color = if (isEqEnabled) SonicCyan else Color(0xFF334155),
                                shape = CircleShape
                            )
                            .clickable { viewModel.toggleEq(!isEqEnabled) }
                            .testTag("master_eq_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Toggle Equalizer",
                            tint = if (isEqEnabled) SonicCyan else SonicTextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bluetooth pill status bar
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SonicSurfaceVariant)
                        .clickable { onNavigateToBluetooth() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth",
                            tint = if (connectedDevice != null) SonicCyan else SonicTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (connectedDevice != null) "Connected: $connectedDevice" else "No Bluetooth device connected",
                            fontSize = 12.sp,
                            fontWeight = if (connectedDevice != null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (connectedDevice != null) SonicTextPrimary else SonicTextMuted
                        )
                    }

                    Text(
                        text = "Manage →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SonicCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Audio Spectrum Visualizer
        SpectrumVisualizer(
            isEqEnabled = isEqEnabled,
            isPlaying = isTestAudioPlaying || activeSessionCount > 0,
            bandLevels = bandLevels
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Test Sound Player Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SonicSurface)
                .border(1.dp, SonicSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isTestAudioPlaying) SonicPink else SonicSurfaceVariant)
                        .clickable { viewModel.toggleTestAudio() }
                        .testTag("test_audio_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isTestAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Stop Test Audio",
                        tint = if (isTestAudioPlaying) SonicBackground else SonicCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Built-in Sound Tester",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SonicTextPrimary
                    )
                    Text(
                        text = if (isTestAudioPlaying) "Playing test groove (Bass/Mid/Treble)..." else "Tap play to test live EQ response",
                        fontSize = 11.sp,
                        color = SonicTextSecondary
                    )
                }
            }

            IconButton(onClick = { viewModel.resetToFlat() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset EQ to Flat",
                    tint = SonicTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Chips Row
        Text(
            text = "SOUND PRESETS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SonicTextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        PresetChipRow(
            presets = presets,
            activePreset = activePreset,
            onPresetSelected = { viewModel.selectPreset(it) },
            onSaveNewPresetClicked = { showNewPresetDialog = true },
            onDeletePresetClicked = { viewModel.deletePreset(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-Band Equalizer Sliders Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SonicSurface)
                .border(1.dp, SonicSurfaceBorder, RoundedCornerShape(24.dp))
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Equalizer",
                            tint = SonicCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "5-Band Graphic Equalizer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SonicTextPrimary
                        )
                    }

                    Text(
                        text = "±15 dB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SonicCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val frequencies = viewModel.bandFrequencies
                    val minMb = viewModel.minBandLevel.toInt()
                    val maxMb = viewModel.maxBandLevel.toInt()

                    bandLevels.forEachIndexed { index, level ->
                        val freq = if (index < frequencies.size) frequencies[index] else 1000
                        VerticalEqSlider(
                            bandIndex = index,
                            frequencyHz = freq,
                            currentLevelMb = level,
                            minLevelMb = minMb,
                            maxLevelMb = maxMb,
                            isEnabled = isEqEnabled,
                            onLevelChanged = { newLevel ->
                                viewModel.setBandLevel(index, newLevel)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio Effects Section (Bass Boost, Virtualizer, Loudness)
        Text(
            text = "AUDIO EFFECTS & ENHANCERS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SonicTextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bass Boost Knob
            EffectKnobCard(
                title = "Bass Boost",
                value = bassBoost,
                maxValue = 1000,
                accentColor = SonicPink,
                icon = Icons.Default.MusicNote,
                unitSuffix = "%",
                displayMultiplier = 0.1f,
                isEnabled = isEqEnabled,
                onValueChanged = { viewModel.setBassBoost(it) },
                modifier = Modifier.weight(1f),
                testTag = "bass_boost_knob"
            )

            // 3D Virtualizer Knob
            EffectKnobCard(
                title = "3D Surround",
                value = virtualizer,
                maxValue = 1000,
                accentColor = SonicPurple,
                icon = Icons.Default.SurroundSound,
                unitSuffix = "%",
                displayMultiplier = 0.1f,
                isEnabled = isEqEnabled,
                onValueChanged = { viewModel.setVirtualizer(it) },
                modifier = Modifier.weight(1f),
                testTag = "virtualizer_knob"
            )

            // Loudness Enhancer Knob
            EffectKnobCard(
                title = "Loudness",
                value = loudness,
                maxValue = 1000,
                accentColor = SonicAmber,
                icon = Icons.Default.VolumeUp,
                unitSuffix = "%",
                displayMultiplier = 0.1f,
                isEnabled = isEqEnabled,
                onValueChanged = { viewModel.setLoudness(it) },
                modifier = Modifier.weight(1f),
                testTag = "loudness_knob"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Save Preset Dialog
    if (showNewPresetDialog) {
        AlertDialog(
            onDismissRequest = { showNewPresetDialog = false },
            title = { Text(text = "Save Custom Preset", color = SonicTextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Enter a name for your custom EQ curve:",
                        color = SonicTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        placeholder = { Text("e.g. My Bassline, Studio Clean") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            viewModel.saveCurrentAsNewPreset(newPresetName)
                            newPresetName = ""
                            showNewPresetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SonicCyan, contentColor = SonicBackground)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPresetDialog = false }) {
                    Text("Cancel", color = SonicTextSecondary)
                }
            },
            containerColor = SonicSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
