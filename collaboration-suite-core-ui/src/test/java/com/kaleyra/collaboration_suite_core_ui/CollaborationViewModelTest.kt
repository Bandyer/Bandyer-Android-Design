package com.kaleyra.collaboration_suite_core_ui

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.Theme.Style
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), mockk(), mockk(), mockk()) }) {}
        assert(viewModel.isCollaborationConfigured.first())
    }

    @Test
    fun configurationSuccessful_getPhoneBox_getPhoneBoxInstance() = runTest {
        val phoneBox = mockk<PhoneBoxUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(phoneBox, mockk(), mockk(), mockk(), mockk()) }) {}
        assertEquals(viewModel.phoneBox.first(), phoneBox)
    }

    @Test
    fun configurationSuccessful_getChatBox_getChatBoxInstance() = runTest {
        val chatBox = mockk<ChatBoxUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), chatBox, mockk(), mockk(), mockk()) }) {}
        assertEquals(viewModel.chatBox.first(), chatBox)
    }

    @Test
    fun configurationSuccessful_getUsersDescription_getUsersDescriptionInstance() = runTest {
        val usersDescription = mockk<UsersDescription>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), mockk(), mockk(), usersDescription) }) {}
        assertEquals(viewModel.usersDescription.first(), usersDescription)
    }

    @Test
    fun configurationSuccessful_getCompanyName_getCompanyNameInstance() = runTest {
        val companyName = MutableStateFlow("Kaleyra")
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), companyName = companyName, mockk(relaxed = true), mockk()) }) {}
        assertEquals(viewModel.companyName.first(), companyName.value)
    }

    @Test
    fun configurationSuccessful_getTheme_getThemeInstance() = runTest {
        val theme = MutableStateFlow(Theme(day = Style(mockk()), night = Style(mockk())))
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), mockk(relaxed = true), theme = theme, mockk()) }) {}
        assertEquals(viewModel.theme.first(), theme.value)
    }
}