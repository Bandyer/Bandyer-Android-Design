package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite_core_ui.Theme.Style
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollaborationViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    @Test
    fun configurationFailed_isCollaborationConfigured_false() = runTest {
        val viewModel = object : CollaborationViewModel({ Configuration.Failure }) {}
        assert(!viewModel.isCollaborationConfigured.first())
    }

    @Test
    fun configurationSuccessful_isCollaborationConfigured_true() = runTest {
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), mockk(), mockk()) }) {}
        assert(viewModel.isCollaborationConfigured.first())
    }

    @Test
    fun configurationSuccessful_getPhoneBox_getPhoneBoxInstance() = runTest {
        val phoneBox = mockk<PhoneBoxUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(phoneBox, mockk(), mockk(), mockk()) }) {}
        assertEquals(viewModel.phoneBox.first(), phoneBox)
    }

    @Test
    fun configurationSuccessful_getChatBox_getChatBoxInstance() = runTest {
        val chatBox = mockk<ChatBoxUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), chatBox, mockk(), mockk()) }) {}
        assertEquals(viewModel.chatBox.first(), chatBox)
    }

    @Test
    fun configurationSuccessful_getCompanyName_getCompanyNameInstance() = runTest {
        val company = mockk<CompanyUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), company = company, mockk(relaxed = true)) }) {}
        assertEquals(viewModel.company.first(), company)
    }

//    @Test
//    fun configurationSuccessful_getTheme_getThemeInstance() = runTest {
//        val theme = MutableStateFlow(Theme(day = Style(mockk()), night = Style(mockk())))
//        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), mockk(relaxed = true), theme = theme) }) {}
//        assertEquals(viewModel.theme.first(), theme.value)
//    }
}