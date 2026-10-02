package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SonicCyan
import com.example.ui.theme.SonicSurface
import com.example.ui.theme.SonicSurfaceBorder
import com.example.ui.theme.SonicSurfaceVariant
import com.example.ui.theme.SonicTextMuted
import com.example.ui.theme.SonicTextPrimary
import com.example.ui.theme.SonicTextSecondary
import kotlin.math.roundToInt

@Composable
fun EffectKnobCard(
    title: String,
    value: Int, // 0 to 1000
    maxValue: Int = 1000,
    accentColor: Color,
    icon: ImageVector,
    unitSuffix: String = "%",
    displayMultiplier: Float = 0.1f, // 1000 -> 100%
    isEnabled: Boolean,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val progress = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    val displayValue = (value * displayMultiplier / 10f).roundToInt()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SonicSurface)
            .border(1.dp, SonicSurfaceBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isEnabled && value > 0) accentColor else SonicTextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SonicTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Circular dial knob
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .pointerInput(isEnabled) {
                        if (!isEnabled) return@pointerInput
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Drag up/right increases, down/left decreases
                            val delta = (-dragAmount.y + dragAmount.x) * 4f
                            val newValue = (value + delta.roundToInt()).coerceIn(0, maxValue)
                            onValueChanged(newValue)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val startAngle = 135f
                    val sweepAngle = 270f

                    // Background track arc
                    drawArc(
                        color = Color(0xFF23283E),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active progress arc
                    if (progress > 0.01f) {
                        drawArc(
                            brush = if (isEnabled) {
                                Brush.sweepGradient(
                                    listOf(accentColor.copy(alpha = 0.6f), accentColor)
                                )
                            } else {
                                Brush.sweepGradient(listOf(Color(0xFF64748B), Color(0xFF475569)))
                            },
                            startAngle = startAngle,
                            sweepAngle = sweepAngle * progress,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center read-out
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$displayValue$unitSuffix",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isEnabled && value > 0) accentColor else SonicTextMuted
                    )
                    Text(
                        text = if (value == 0) "OFF" else "ON",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = SonicTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick level shortcuts (0%, 50%, 100%)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(0, (maxValue * 0.4f).roundToInt(), (maxValue * 0.75f).roundToInt(), maxValue).forEach { stepVal ->
                    val label = when (stepVal) {
                        0 -> "0"
                        maxValue -> "MAX"
                        else -> "${(stepVal * displayMultiplier / 10f).roundToInt()}"
                    }
                    val isSelected = kotlin.math.abs(value - stepVal) < 40

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected && isEnabled) accentColor.copy(alpha = 0.2f) else SonicSurfaceVariant)
                            .clickable(enabled = isEnabled) { onValueChanged(stepVal) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected && isEnabled) accentColor else SonicTextSecondary
                        )
                    }
                }
            }
        }
    }
}
