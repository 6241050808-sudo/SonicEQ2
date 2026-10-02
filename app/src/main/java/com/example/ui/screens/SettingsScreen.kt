package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasNotifPermission by remember { mutableStateOf(true) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SonicBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // APK Ready Announcement Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SonicSurface)
                .border(1.dp, SonicGreen.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SonicGreen.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = SonicGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "APK Ready for Installation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SonicTextPrimary
                        )
                        Text(
                            text = "app-debug.apk generated & installable on any Android phone",
                            fontSize = 11.sp,
                            color = SonicTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "You can download the APK directly from AI Studio's settings/export panel, or install via standard Android package installer. No root required.",
                    fontSize = 12.sp,
                    color = SonicTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // Background Service & Notifications
        Text(
            text = "SYSTEM & PERMISSIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SonicTextMuted,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SonicSurface)
                .border(1.dp, SonicSurfaceBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Notification Permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = SonicCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Foreground Notification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SonicTextPrimary
                            )
                            Text(
                                text = "Keeps EQ active when streaming in background",
                                fontSize = 11.sp,
                                color = SonicTextMuted
                            )
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {
                                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SonicSurfaceVariant, contentColor = SonicCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Battery Optimization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = SonicPink,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ignore Battery Optimization",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SonicTextPrimary
                            )
                            Text(
                                text = "Prevents Android from killing EQ during screen off",
                                fontSize = 11.sp,
                                color = SonicTextMuted
                            )
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SonicSurfaceVariant, contentColor = SonicCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Config", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Audio Engine Specifications Card
        Text(
            text = "AUDIO ENGINE SPECIFICATIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SonicTextMuted,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SonicSurface)
                .border(1.dp, SonicSurfaceBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineSpecRow("DSP Engine", "Android OpenMAX / HAL AudioFX")
                EngineSpecRow("Bands Supported", "5 Hardware Bands (±15 dB range)")
                EngineSpecRow("Bass Processing", "Dynamic BassBoost (0 - 1000 mB)")
                EngineSpecRow("Spatial Audio", "3D Virtualizer / Room Reverberation")
                EngineSpecRow("Gain Enhancer", "Digital Loudness Enhancer (+10 dB)")
                EngineSpecRow("Bluetooth Sync", "Auto-Detect ACL & A2DP Audio Profiles")
                EngineSpecRow("Streaming Support", "Spotify, YT Music, Apple, Tidal, Deezer")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun EngineSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = SonicTextSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = SonicTextPrimary
        )
    }
}
