/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
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
        viewModel = BaseViewModelImpl { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
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