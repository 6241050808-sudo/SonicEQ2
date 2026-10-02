package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SonicCyan
import com.example.ui.theme.SonicPurple
import com.example.ui.theme.SonicSliderTrack
import com.example.ui.theme.SonicSurfaceVariant
import com.example.ui.theme.SonicTextMuted
import com.example.ui.theme.SonicTextPrimary
import kotlin.math.roundToInt

@Composable
fun VerticalEqSlider(
    bandIndex: Int,
    frequencyHz: Int,
    currentLevelMb: Int,
    minLevelMb: Int,
    maxLevelMb: Int,
    isEnabled: Boolean,
    onLevelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val frequencyLabel = remember(frequencyHz) {
        if (frequencyHz >= 1_000_000) {
            val khz = frequencyHz / 1_000_000f
            if (khz == khz.toInt().toFloat()) "${khz.toInt()}k" else String.format("%.1fk", khz)
        } else if (frequencyHz >= 1_000) {
            val hz = frequencyHz / 1_000
            "$hz"
        } else {
            "$frequencyHz"
        }
    }

    val dbVal = currentLevelMb / 100f
    val dbLabel = remember(currentLevelMb) {
        when {
            currentLevelMb > 0 -> String.format("+%.1f", dbVal)
            currentLevelMb < 0 -> String.format("%.1f", dbVal)
            else -> "0.0"
        }
    }

    // Normalized progress: 0f (bottom / min) to 1f (top / max)
    val range = (maxLevelMb - minLevelMb).toFloat().coerceAtLeast(1f)
    val progress = ((currentLevelMb - minLevelMb) / range).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(horizontal = 4.dp)
            .testTag("eq_band_$bandIndex")
    ) {
        // dB display
        Text(
            text = dbLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isEnabled) SonicCyan else SonicTextMuted,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Custom vertical drag track
        var trackHeightPx by remember { mutableFloatStateOf(1f) }

        Box(
            modifier = Modifier
                .width(28.dp)
                .height(180.dp)
                .onGloballyPositioned { coordinates ->
                    trackHeightPx = coordinates.size.height.toFloat()
                }
                .draggable(
                    orientation = Orientation.Vertical,
                    enabled = isEnabled,
                    state = rememberDraggableState { deltaY ->
                        if (trackHeightPx > 0) {
                            // Dragging down is positive deltaY (decreasing gain)
                            val deltaProgress = -deltaY / trackHeightPx
                            val newProgress = (progress + deltaProgress).coerceIn(0f, 1f)
                            val newLevel = (minLevelMb + newProgress * range).roundToInt()
                            onLevelChanged(newLevel)
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background track slot
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isEnabled) SonicSliderTrack else Color(0xFF1E212D))
            )

            // Center 0 dB dash indicator
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(Color(0xFF475569))
            )

            // Active fill from center
            val centerProgress = ((-minLevelMb) / range).coerceIn(0f, 1f)
            val fillHeightFraction = kotlin.math.abs(progress - centerProgress)
            val isAboveZero = progress >= centerProgress

            if (fillHeightFraction > 0.01f && isEnabled) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(fillHeightFraction)
                        .align(if (isAboveZero) Alignment.BottomCenter else Alignment.TopCenter)
                        .offset {
                            val offsetY = if (isAboveZero) {
                                -((centerProgress) * trackHeightPx).toInt()
                            } else {
                                ((1f - centerProgress) * trackHeightPx).toInt()
                            }
                            IntOffset(0, offsetY)
                        }
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SonicCyan, SonicPurple)
                            )
                        )
                )
            }

            // Slider thumb knob
            val thumbOffsetY = if (trackHeightPx > 0) {
                ((1f - progress) * (trackHeightPx - 26.dp.value)).toInt()
            } else 0

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, thumbOffsetY) }
                    .size(26.dp)
                    .shadow(elevation = if (isEnabled) 6.dp else 0.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) {
                            Brush.radialGradient(
                                colors = listOf(SonicCyan, Color(0xFF007A8A))
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF64748B), Color(0xFF334155))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Frequency Label
        Text(
            text = frequencyLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) SonicTextPrimary else SonicTextMuted,
            maxLines = 1
        )
    }
}
