package com.kaleyra.collaboration_suite_phone_ui.viewmodel.call

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.ConferenceUI
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.call.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.core.viewmodel.BaseViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private class UiStateImpl() : UiState

    private class BaseViewModelImpl(configure: suspend () -> Configuration) : BaseViewModel<UiStateImpl>(configure) {
        override fun initialState() = UiStateImpl()
        fun getCall(): Flow<Call> {
            return super.call
        }
    }

    private lateinit var viewModel: BaseViewModelImpl

    private val conferenceMock = mockk<ConferenceUI>()


    @Before
    fun setUp() {
        viewModel = BaseViewModelImpl { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true)) }
    }

    @Test
    fun testCallNotUpdatedOnNewConferenceCall() = runTest {
        val callMock1 = mockk<CallUI>()
        val callMock2 = mockk<CallUI>()
        val call = MutableStateFlow(callMock1)
        every { conferenceMock.call } returns call

        val viewModelCall = viewModel.getCall()
        val actual = viewModelCall.first()
        assertEquals(callMock1, actual)

        call.value = callMock2
        val new = viewModelCall.first()
        assertEquals(callMock1, new)
    }
}