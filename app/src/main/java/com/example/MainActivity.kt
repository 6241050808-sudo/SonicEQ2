package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.BluetoothScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StreamingAppsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SonicBackground
import com.example.ui.theme.SonicCyan
import com.example.ui.theme.SonicSurface
import com.example.ui.theme.SonicSurfaceVariant
import com.example.ui.theme.SonicTextMuted
import com.example.ui.theme.SonicTextSecondary
import com.example.viewmodel.EqualizerViewModel

enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    EQUALIZER("Equalizer", Icons.Default.GraphicEq, "nav_tab_equalizer"),
    BLUETOOTH("Bluetooth", Icons.Default.Bluetooth, "nav_tab_bluetooth"),
    STREAMING("Stream Hub", Icons.Default.MusicNote, "nav_tab_streaming"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: EqualizerViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPairedDevices()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestInitialPermissions()
        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                var selectedTabIndex by remember { mutableIntStateOf(0) }

                LaunchedEffect(intent) {
                    if (intent?.hasExtra("EXTRA_CONNECTED_DEVICE") == true) {
                        selectedTabIndex = 1 // Switch to Bluetooth tab
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SonicBackground,
                    bottomBar = {
                        NavigationBar(
                            containerColor = SonicSurface,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavTab.values().forEachIndexed { index, tab ->
                                val isSelected = selectedTabIndex == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTabIndex = index },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SonicCyan,
                                        selectedTextColor = SonicCyan,
                                        unselectedIconColor = SonicTextMuted,
                                        unselectedTextColor = SonicTextMuted,
                                        indicatorColor = SonicSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTabIndex) {
                            0 -> EqualizerScreen(
                                viewModel = viewModel,
                                onNavigateToBluetooth = { selectedTabIndex = 1 }
                            )
                            1 -> BluetoothScreen(viewModel = viewModel)
                            2 -> StreamingAppsScreen(viewModel = viewModel)
                            3 -> SettingsScreen()
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val connectedDevice = intent.getStringExtra("EXTRA_CONNECTED_DEVICE")
        if (!connectedDevice.isNullOrBlank()) {
            SonicEqApplication.instance.bluetoothManager.setConnectedDevice(connectedDevice)
        }
    }

    private fun requestInitialPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
