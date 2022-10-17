package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollaborationViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    @Test
    fun configurationFailed_isCollaborationConfigured_false() = runTest {
        val viewModel = object : CollaborationViewModel({Configuration.Failure}) {}
        assert(!viewModel.isCollaborationConfigured.first())
    }

    @Test
    fun configurationSuccessful_isCollaborationConfigured_true() = runTest {
        val viewModel = object : CollaborationViewModel({Configuration.Success(mockk(), mockk(), mockk())}) {}
        assert(viewModel.isCollaborationConfigured.first())
    }

    @Test
    fun configurationSuccessful_getPhoneBox_getPhoneBoxInstance() = runTest {
        val phoneBox = mockk<PhoneBoxUI>()
        val viewModel = object : CollaborationViewModel({Configuration.Success(phoneBox, mockk(), mockk())}) {}
        assert(viewModel.phoneBox.first() == phoneBox)
    }

    @Test
    fun configurationSuccessful_getChatBox_getChatBoxInstance() = runTest {
        val chatBox = mockk<ChatBoxUI>()
        val viewModel = object : CollaborationViewModel({Configuration.Success(mockk(), chatBox, mockk())}) {}
        assert(viewModel.chatBox.first() == chatBox)
    }

    @Test
    fun configurationSuccessful_getUsersDescription_getUsersDescriptionInstance() = runTest {
        val usersDescription = mockk<UsersDescription>()
        val viewModel = object : CollaborationViewModel({Configuration.Success(mockk(), mockk(), usersDescription)}) {}
        assert(viewModel.usersDescription.first() == usersDescription)
    }
}