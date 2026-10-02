package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Converters
import com.example.model.EqPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SonicEQ", appName)
    }

    @Test
    fun `default presets contain expected audio profiles`() {
        val presets = EqPreset.defaultPresets()
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.any { it.name.startsWith("Flat") })
        assertTrue(presets.any { it.name.startsWith("Bass Booster") })
        assertTrue(presets.any { it.name.startsWith("Rock") })
    }

    @Test
    fun `converters serialize and deserialize list of ints`() {
        val converters = Converters()
        val original = listOf(600, 300, 0, -200, 450)
        val serialized = converters.fromIntList(original)
        val deserialized = converters.toIntList(serialized)
        assertEquals(original, deserialized)
    }
}
