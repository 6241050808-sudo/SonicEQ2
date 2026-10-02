package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.BluetoothDeviceConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothDeviceDao {
    @Query("SELECT * FROM bluetooth_configs ORDER BY lastConnectedTimestamp DESC, deviceName ASC")
    fun getAllDevices(): Flow<List<BluetoothDeviceConfig>>

    @Query("SELECT * FROM bluetooth_configs WHERE deviceKey = :key OR UPPER(deviceName) = UPPER(:name) LIMIT 1")
    suspend fun findDevice(key: String, name: String): BluetoothDeviceConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDevice(device: BluetoothDeviceConfig)

    @Update
    suspend fun updateDevice(device: BluetoothDeviceConfig)

    @Delete
    suspend fun deleteDevice(device: BluetoothDeviceConfig)

    @Query("DELETE FROM bluetooth_configs WHERE deviceKey = :key")
    suspend fun deleteByKey(key: String)
}
