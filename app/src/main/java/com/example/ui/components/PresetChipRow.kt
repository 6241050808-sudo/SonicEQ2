package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqPreset
import com.example.ui.theme.SonicCyan
import com.example.ui.theme.SonicPurple
import com.example.ui.theme.SonicSurface
import com.example.ui.theme.SonicSurfaceBorder
import com.example.ui.theme.SonicSurfaceVariant
import com.example.ui.theme.SonicTextMuted
import com.example.ui.theme.SonicTextPrimary
import com.example.ui.theme.SonicTextSecondary

@Composable
fun PresetChipRow(
    presets: List<EqPreset>,
    activePreset: EqPreset?,
    onPresetSelected: (EqPreset) -> Unit,
    onSaveNewPresetClicked: () -> Unit,
    onDeletePresetClicked: (EqPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("preset_chips_row")
    ) {
        // "+ New" chip
        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SonicSurfaceVariant)
                    .border(1.dp, SonicCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onSaveNewPresetClicked() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("add_custom_preset_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Preset",
                        tint = SonicCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Preset",
                        color = SonicCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // List of presets
        items(presets, key = { it.id }) { preset ->
            val isSelected = activePreset?.id == preset.id

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) SonicCyan.copy(alpha = 0.18f) else SonicSurface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) SonicCyan else SonicSurfaceBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onPresetSelected(preset) }
                    .padding(start = 14.dp, end = if (preset.isCustom) 6.dp else 14.dp, top = 8.dp, bottom = 8.dp)
                    .testTag("preset_chip_${preset.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SonicCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = preset.name,
                        color = if (isSelected) SonicCyan else SonicTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )

                    if (preset.isCustom) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155))
                                .clickable { onDeletePresetClicked(preset) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete ${preset.name}",
                                tint = SonicTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
