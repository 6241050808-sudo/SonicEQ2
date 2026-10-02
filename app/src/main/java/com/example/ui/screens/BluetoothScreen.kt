package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BluetoothDeviceConfig
import com.example.model.EqPreset
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
fun BluetoothScreen(
    viewModel: EqualizerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedDevices by viewModel.savedBluetoothDevices.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val connectedDevice by viewModel.connectedBluetoothDevice.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.refreshPairedDevices()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SonicBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Status Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SonicSurface)
                    .border(1.dp, if (connectedDevice != null) SonicCyan.copy(alpha = 0.5f) else SonicSurfaceBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp)
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
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (connectedDevice != null) SonicCyan.copy(alpha = 0.2f) else SonicSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (connectedDevice != null) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                contentDescription = "Bluetooth Status",
                                tint = if (connectedDevice != null) SonicCyan else SonicTextMuted,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (connectedDevice != null) "Connected" else "Waiting for Connection",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (connectedDevice != null) SonicGreen else SonicTextMuted
                                )
                                if (connectedDevice != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SonicGreen)
                                    )
                                }
                            }
                            Text(
                                text = connectedDevice ?: "No Bluetooth Audio Active",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SonicTextPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.refreshPairedDevices() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh paired devices",
                            tint = SonicCyan
                        )
                    }
                }
            }
        }

        // Action header & Add button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SAVED BLUETOOTH PROFILES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SonicTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Auto-switches EQ & launches when connected",
                        fontSize = 12.sp,
                        color = SonicTextSecondary
                    )
                }

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            btPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
                        }
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SonicCyan, contentColor = SonicBackground),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_bt_device_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Device",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Device", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Empty state
        if (savedDevices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SonicSurface)
                        .border(1.dp, SonicSurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "No saved devices",
                            tint = SonicTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Bluetooth Device Configured",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SonicTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your car audio, headphones, or earbuds. When connected, SonicEQ automatically applies your custom EQ preset!",
                            fontSize = 12.sp,
                            color = SonicTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Saved devices list
        items(savedDevices, key = { it.deviceKey }) { config ->
            val isCurrent = connectedDevice?.equals(config.deviceName, ignoreCase = true) == true
            val assignedPreset = presets.find { it.id == config.assignedPresetId } ?: presets.firstOrNull()

            var showPresetMenu by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SonicSurface)
                    .border(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = if (isCurrent) SonicCyan else SonicSurfaceBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
                    .testTag("saved_device_${config.deviceKey}")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent) SonicGreen else Color(0xFF475569))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = config.deviceName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SonicTextPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteBluetoothDevice(config) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete device config",
                                tint = SonicTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Assigned Preset selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Assigned EQ Profile:",
                            fontSize = 12.sp,
                            color = SonicTextSecondary
                        )

                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SonicSurfaceVariant)
                                    .border(1.dp, SonicCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable { showPresetMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Select preset",
                                        tint = SonicCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = assignedPreset?.name ?: "Flat",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SonicCyan
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showPresetMenu,
                                onDismissRequest = { showPresetMenu = false },
                                modifier = Modifier.background(SonicSurfaceVariant)
                            ) {
                                presets.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p.name, color = SonicTextPrimary) },
                                        onClick = {
                                            viewModel.saveBluetoothDevice(
                                                key = config.deviceKey,
                                                name = config.deviceName,
                                                assignedPresetId = p.id,
                                                autoStartApp = config.autoStartApp,
                                                autoEnableEq = config.autoEnableEq
                                            )
                                            showPresetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggles Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto-Start App",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SonicTextPrimary
                            )
                            Text(
                                text = "Launch app/alert on connect",
                                fontSize = 11.sp,
                                color = SonicTextMuted
                            )
                        }

                        Switch(
                            checked = config.autoStartApp,
                            onCheckedChange = { checked ->
                                viewModel.saveBluetoothDevice(
                                    key = config.deviceKey,
                                    name = config.deviceName,
                                    assignedPresetId = config.assignedPresetId,
                                    autoStartApp = checked,
                                    autoEnableEq = config.autoEnableEq
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SonicCyan,
                                checkedTrackColor = SonicCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = SonicTextMuted,
                                uncheckedTrackColor = SonicSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto-Enable EQ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SonicTextPrimary
                            )
                            Text(
                                text = "Turn on sound processing instantly",
                                fontSize = 11.sp,
                                color = SonicTextMuted
                            )
                        }

                        Switch(
                            checked = config.autoEnableEq,
                            onCheckedChange = { checked ->
                                viewModel.saveBluetoothDevice(
                                    key = config.deviceKey,
                                    name = config.deviceName,
                                    assignedPresetId = config.assignedPresetId,
                                    autoStartApp = config.autoStartApp,
                                    autoEnableEq = checked
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SonicCyan,
                                checkedTrackColor = SonicCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = SonicTextMuted,
                                uncheckedTrackColor = SonicSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }

    // Add Device Dialog
    if (showAddDialog) {
        var inputName by remember { mutableStateOf("") }
        var selectedPresetId by remember { mutableLongStateOf(presets.firstOrNull()?.id ?: 1L) }
        var autoStart by remember { mutableStateOf(true) }
        var autoEq by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Bluetooth Audio Device",
                    color = SonicTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pick from paired devices or type custom name:",
                        color = SonicTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick chip selector of paired devices
                    if (pairedDevices.isNotEmpty()) {
                        Text(
                            text = "Paired on this phone:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SonicCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        pairedDevices.take(4).forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inputName = p.name }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = SonicCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = p.name,
                                    fontSize = 12.sp,
                                    color = if (inputName == p.name) SonicCyan else SonicTextPrimary,
                                    fontWeight = if (inputName == p.name) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Device Name") },
                        placeholder = { Text("e.g. Sony WH-1000XM5, Car Stereo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Preset to apply when connected:",
                        fontSize = 12.sp,
                        color = SonicTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Preset selection row
                    Column {
                        presets.take(5).forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPresetId = p.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedPresetId == p.id) SonicCyan else SonicSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedPresetId == p.id) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(SonicBackground)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = p.name,
                                    fontSize = 13.sp,
                                    color = if (selectedPresetId == p.id) SonicCyan else SonicTextPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            viewModel.saveBluetoothDevice(
                                key = inputName.trim(),
                                name = inputName.trim(),
                                assignedPresetId = selectedPresetId,
                                autoStartApp = autoStart,
                                autoEnableEq = autoEq
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SonicCyan, contentColor = SonicBackground)
                ) {
                    Text("Save Device")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = SonicTextSecondary)
                }
            },
            containerColor = SonicSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
