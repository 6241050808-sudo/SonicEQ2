package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bluetooth_configs")
data class BluetoothDeviceConfig(
    @PrimaryKey
    val deviceKey: String, // MAC address or clean device name
    val deviceName: String,
    val assignedPresetId: Long,
    val autoStartApp: Boolean = true,
    val autoEnableEq: Boolean = true,
    val lastConnectedTimestamp: Long = 0L
)
