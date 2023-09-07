package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.ConfigurationExtensions.MediumSizeHeight
import com.kaleyra.collaboration_suite_phone_ui.call.ConfigurationExtensions.MediumSizeWidth
import com.kaleyra.collaboration_suite_phone_ui.call.ConfigurationExtensions.isAtLeastMediumSizeDevice
import com.kaleyra.collaboration_suite_phone_ui.call.ConfigurationExtensions.isAtLeastMediumSizeHeight
import com.kaleyra.collaboration_suite_phone_ui.call.ConfigurationExtensions.isAtLeastMediumSizeWidth
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigurationExtensionsTest {

    @Test
    fun testIsAtLeastMediumSizeHeight() {
        assertEquals(true, MediumSizeHeight.isAtLeastMediumSizeHeight())
        assertEquals(true, (MediumSizeHeight + 50.dp).isAtLeastMediumSizeHeight())
        assertEquals(false, (MediumSizeHeight - 50.dp).isAtLeastMediumSizeHeight())
    }

    @Test
    fun testIsAtLeastMediumSizeWeight() {
        assertEquals(true, MediumSizeWidth.isAtLeastMediumSizeWidth())
        assertEquals(true, (MediumSizeWidth + 50.dp).isAtLeastMediumSizeWidth())
        assertEquals(false, (MediumSizeWidth - 50.dp).isAtLeastMediumSizeWidth())
    }

    @Test
    fun testIsAtLeastMediumSizeDevice() {
        assertEquals(true, isAtLeastMediumSizeDevice(MediumSizeWidth, MediumSizeHeight))
        assertEquals(false, isAtLeastMediumSizeDevice(MediumSizeWidth - 50.dp, MediumSizeHeight))
        assertEquals(true, isAtLeastMediumSizeDevice(MediumSizeWidth + 50.dp, MediumSizeHeight))
        assertEquals(false, isAtLeastMediumSizeDevice(MediumSizeWidth, MediumSizeHeight - 50.dp))
        assertEquals(true, isAtLeastMediumSizeDevice(MediumSizeWidth, MediumSizeHeight + 50.dp))
        assertEquals(false, isAtLeastMediumSizeDevice(MediumSizeWidth - 50.dp, MediumSizeHeight - 50.dp))
        assertEquals(true, isAtLeastMediumSizeDevice(MediumSizeWidth + 50.dp, MediumSizeHeight + 50.dp))
    }
}