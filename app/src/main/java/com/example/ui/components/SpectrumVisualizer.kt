package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SonicCyan
import com.example.ui.theme.SonicPink
import com.example.ui.theme.SonicPurple
import com.example.ui.theme.SonicSurface
import kotlin.math.sin

@Composable
fun SpectrumVisualizer(
    isEqEnabled: Boolean,
    isPlaying: Boolean,
    bandLevels: List<Int>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SonicSurface.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val barCount = 28
            val totalSpacing = (barCount - 1) * 4.dp.toPx()
            val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(3f)
            val maxHeight = size.height

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(SonicPink, SonicPurple, SonicCyan),
                startY = 0f,
                endY = maxHeight
            )

            for (i in 0 until barCount) {
                val normalizedBandIdx = (i * bandLevels.size) / barCount
                val bandGainMb = if (bandLevels.isNotEmpty() && normalizedBandIdx < bandLevels.size) {
                    bandLevels[normalizedBandIdx]
                } else {
                    0
                }
                // Convert gain to factor (0.2 to 1.8)
                val gainFactor = ((bandGainMb + 1500) / 3000f).coerceIn(0.1f, 1.0f)

                val barHeight = if (isEqEnabled && isPlaying) {
                    val wave = (sin(phase + i * 0.45) + 1.0) / 2.0
                    val wave2 = (sin(phase * 1.5 - i * 0.3) + 1.0) / 2.0
                    val combined = (wave * 0.6 + wave2 * 0.4).toFloat()
                    (maxHeight * (0.25f + 0.70f * combined * gainFactor)).coerceIn(6f, maxHeight)
                } else if (isEqEnabled) {
                    // Gentle breathing idle state
                    val idle = ((sin(phase * 0.5 + i * 0.2) + 1.0) / 2.0).toFloat()
                    (maxHeight * (0.12f + 0.25f * idle * gainFactor)).coerceIn(4f, maxHeight * 0.5f)
                } else {
                    // Bypassed state: flat low line
                    4.dp.toPx()
                }

                val x = i * (barWidth + 4.dp.toPx())
                val y = maxHeight - barHeight

                drawRoundRect(
                    brush = if (isEqEnabled) gradientBrush else Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF334155))),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}
