package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.Company
import com.kaleyra.collaboration_suite_core_ui.Theme.Style
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

//@OptIn(ExperimentalCoroutinesApi::class)
//class CompanyUITest {
//
//    val dayStyle = Style(mockk())
//    val nightStyle = Style(mockk())
//
//    @Test
//    fun `remote company details`() = runTest {
//        val mockTheme = Theme(dayStyle, nightStyle)
//        val companyCollab = mockk<Company>(relaxed = true) {
//            every { theme } returns MutableStateFlow(mockTheme)
//        }
//        val company = CompanyUI(companyCollab, backgroundScope)
//        assertEquals(mockTheme, company.theme.first())
//    }
//
//    @Test
//    fun `set local company details`() = runTest {
//        val mockRemote = MutableSharedFlow<Company.Theme>()
//        val remoteTheme = Theme(mockk(relaxed = true), mockk(relaxed = true))
//        val localTheme = Theme(dayStyle, nightStyle)
//        val companyCollab = mockk<Company>(relaxed = true) {
//            every { theme } returns mockRemote
//        }
//        val company = CompanyUI(companyCollab, backgroundScope)
//        company.setTheme(localTheme)
//        assertEquals(localTheme, company.theme.first())
//        mockRemote.emit(remoteTheme)
//        advanceUntilIdle()
//        assertEquals(localTheme, company.theme.first())
//    }
//}