package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StreamingApp
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
fun StreamingAppsScreen(
    viewModel: EqualizerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val streamingApps by viewModel.streamingApps.collectAsState()
    val activeSessionCount by viewModel.activeSessionCount.collectAsState()
    val lastActivePackage by viewModel.lastActivePackage.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SonicBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Integration Guide Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SonicSurface)
                    .border(1.dp, SonicCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SonicCyan.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = SonicCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Universal Streaming Support",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SonicTextPrimary
                            )
                            Text(
                                text = "Auto-detects Spotify, YouTube Music, Apple Music & more",
                                fontSize = 11.sp,
                                color = SonicTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // How-To Steps
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepItem(
                            stepNumber = "1",
                            text = "Keep SonicEQ active (switch ON at top of Equalizer tab)."
                        )
                        StepItem(
                            stepNumber = "2",
                            text = "In Spotify or YT Music, go to Settings → Equalizer, and select SonicEQ."
                        )
                        StepItem(
                            stepNumber = "3",
                            text = "SonicEQ dynamically captures the music audio session and applies your custom EQ & Bass Boost live!"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SonicSurfaceVariant, contentColor = SonicCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open System Sound Settings",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "SUPPORTED MUSIC APPS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SonicTextMuted,
                letterSpacing = 1.sp
            )
        }

        // Apps Grid/List
        items(streamingApps, key = { it.id }) { app ->
            val isCurrentStream = lastActivePackage?.contains(app.packageName, ignoreCase = true) == true

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SonicSurface)
                    .border(
                        width = if (isCurrentStream) 1.5.dp else 1.dp,
                        color = if (isCurrentStream) SonicCyan else SonicSurfaceBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
                    .testTag("streaming_app_${app.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // App Brand Indicator
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(app.brandColor.copy(alpha = 0.2f))
                                .border(1.dp, app.brandColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = app.name.take(2).uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = app.brandColor
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SonicTextPrimary
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                if (isCurrentStream) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SonicGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PLAYING NOW",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SonicGreen
                                        )
                                    }
                                } else if (app.isInstalled) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SonicCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "INSTALLED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SonicCyan
                                        )
                                    }
                                }
                            }

                            Text(
                                text = app.description,
                                fontSize = 11.sp,
                                color = SonicTextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    // Action Button
                    Button(
                        onClick = { StreamingApp.launchOrOpenStore(context, app) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (app.isInstalled) SonicCyan else SonicSurfaceVariant,
                            contentColor = if (app.isInstalled) SonicBackground else SonicCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (app.isInstalled) Icons.Default.OpenInNew else Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (app.isInstalled) "Launch" else "Get",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(stepNumber: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(SonicCyan.copy(alpha = 0.2f))
        ) {
            Text(
                text = stepNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SonicCyan
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = SonicTextSecondary,
            lineHeight = 16.sp
        )
    }
}
