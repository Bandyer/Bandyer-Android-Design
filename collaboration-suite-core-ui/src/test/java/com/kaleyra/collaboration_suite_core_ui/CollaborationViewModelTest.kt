package com.kaleyra.collaboration_suite_core_ui

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), mockk()) }) {}
        assert(viewModel.isCollaborationConfigured.first())
    }

    @Test
    fun configurationSuccessful_getConference_getConferenceInstance() = runTest {
        val conference = mockk<ConferenceUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(conference, mockk(), mockk()) }) {}
        assertEquals(viewModel.conference.first(), conference)
    }

    @Test
    fun configurationSuccessful_getChatBox_getChatBoxInstance() = runTest {
        val chatBox = mockk<ChatBoxUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), chatBox, mockk()) }) {}
        assertEquals(viewModel.chatBox.first(), chatBox)
    }

    @Test
    fun configurationSuccessful_getCompanyName_getCompanyNameInstance() = runTest {
        val company = mockk<CompanyUI>()
        val viewModel = object : CollaborationViewModel({ Configuration.Success(mockk(), mockk(), company = company) }) {}
        assertEquals(viewModel.company.first(), company)
    }
}