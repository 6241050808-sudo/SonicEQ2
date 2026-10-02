package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.EqPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM eq_presets ORDER BY id ASC")
    fun getAllPresets(): Flow<List<EqPreset>>

    @Query("SELECT * FROM eq_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: Long): EqPreset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqPreset): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(presets: List<EqPreset>)

    @Update
    suspend fun updatePreset(preset: EqPreset)

    @Delete
    suspend fun deletePreset(preset: EqPreset)

    @Query("SELECT COUNT(*) FROM eq_presets")
    suspend fun getPresetCount(): Int
}
