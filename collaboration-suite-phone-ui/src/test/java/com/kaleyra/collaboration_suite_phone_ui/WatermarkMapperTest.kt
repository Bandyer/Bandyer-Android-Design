package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Logo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.WatermarkInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WatermarkMapperTest {

    private val companyMock = mockk<Company>()

    private val themeMock = mockk<Company.Theme>()

    private val dayLogo = mockk<Uri>()

    private val nightLogo = mockk<Uri>()

    @Before
    fun setUp() {
        with(themeMock) {
            every { day } returns mockk {
                every { logo } returns dayLogo
            }
            every { night } returns mockk {
                every { logo } returns nightLogo
            }
        }
        with(companyMock) {
            every { name } returns MutableStateFlow("companyName")
            every { theme } returns MutableStateFlow(themeMock)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun companyTheme_toWatermarkInfo_watermarkInfo() = runTest {
        val result = flowOf(companyMock).toWatermarkInfo()
        val actual = result.first()
        val expected = WatermarkInfo(text = "companyName", logo = Logo(dayLogo, nightLogo))
        assertEquals(expected, actual)
    }
}