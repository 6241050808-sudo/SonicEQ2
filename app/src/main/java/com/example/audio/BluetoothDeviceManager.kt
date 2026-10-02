package com.example.audio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.model.BluetoothDeviceConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DiscoveredBluetoothDevice(
    val address: String,
    val name: String,
    val isConnected: Boolean
)

class BluetoothDeviceManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _isBluetoothSupported = MutableStateFlow(bluetoothAdapter != null)
    val isBluetoothSupported: StateFlow<Boolean> = _isBluetoothSupported.asStateFlow()

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<DiscoveredBluetoothDevice> {
        if (!hasBluetoothPermission() || bluetoothAdapter == null) {
            return emptyList()
        }
        return try {
            val bonded = bluetoothAdapter.bondedDevices ?: emptySet()
            bonded.map { device ->
                DiscoveredBluetoothDevice(
                    address = device.address,
                    name = device.name ?: "Unknown Device",
                    isConnected = isDeviceConnected(device)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        // Simple heuristic; Android does not have a single public isConnected without BluetoothProfile proxy
        // We will update connected status dynamically via broadcast receiver
        return device.name == _connectedDeviceName.value || device.address == _connectedDeviceName.value
    }

    fun setConnectedDevice(name: String?) {
        _connectedDeviceName.value = name
    }
}
