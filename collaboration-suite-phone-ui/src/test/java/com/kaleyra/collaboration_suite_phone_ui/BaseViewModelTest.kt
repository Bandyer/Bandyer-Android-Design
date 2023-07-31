package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
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

    private val phoneBoxMock = mockk<PhoneBoxUI>()


    @Before
    fun setUp() {
        viewModel = BaseViewModelImpl { Configuration.Success(phoneBoxMock, mockk(), mockk(relaxed = true), mockk(relaxed = true)) }
    }

    @Test
    fun testCallNotUpdatedOnNewPhoneBoxCall() = runTest {
        val callMock1 = mockk<CallUI>()
        val callMock2 = mockk<CallUI>()
        val call = MutableStateFlow(callMock1)
        every { phoneBoxMock.call } returns call

        val viewModelCall = viewModel.getCall()
        val actual = viewModelCall.first()
        assertEquals(callMock1, actual)

        call.value = callMock2
        val new = viewModelCall.first()
        assertEquals(callMock1, new)
    }
}