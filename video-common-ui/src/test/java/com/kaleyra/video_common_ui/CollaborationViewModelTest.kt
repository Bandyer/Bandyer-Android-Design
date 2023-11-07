package com.kaleyra.video_common_ui

import com.kaleyra.video.User
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
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), mockk(), MutableStateFlow(mockk())) }) {}
        assert(viewModel.isCollaborationConfigured.first())
    }

    @Test
    fun configurationSuccessful_getConference_getConferenceInstance() = runTest {
        val conference = mockk<ConferenceUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(conference, mockk(), mockk(), MutableStateFlow(mockk())) }) {}
        assertEquals(viewModel.conference.first(), conference)
    }

    @Test
    fun configurationSuccessful_getConversation_getConversationInstance() = runTest {
        val conversation = mockk<ConversationUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), conversation, mockk(), MutableStateFlow(mockk())) }) {}
        assertEquals(viewModel.conversation.first(), conversation)
    }

    @Test
    fun configurationSuccessful_getCompanyName_getCompanyNameInstance() = runTest {
        val company = mockk<CompanyUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), company = company, MutableStateFlow(mockk())) }) {}
        assertEquals(viewModel.company.first(), company)
    }

    @Test
    fun configurationSuccessful_getConnectedUser_getConnectedUserInstance() = runTest {
        val user = mockk<User>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), mockk(), MutableStateFlow(user)) }) {}
        assertEquals(viewModel.connectedUser.first(), user)
    }
}