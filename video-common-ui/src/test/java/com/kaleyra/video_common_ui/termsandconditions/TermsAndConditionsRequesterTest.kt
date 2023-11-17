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

package com.kaleyra.video_common_ui.termsandconditions

import com.kaleyra.video.State
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TermsAndConditionsRequesterTest {

    @Test
    fun `session state job is cancelled on session authenticated state`() = runTest {
        withTimeout(100) {
            val requester = TermsAndConditionsRequester(this::class.java, this)
            requester.setUp(MutableStateFlow(State.Connected)) {}
        }
    }

//    @Test
//    fun `dispose cancels session state job`() = runTest {
//        withTimeout(100) {
//            val requester = TermsAndConditionsRequester(this::class.java, {}, {}, this)
//            val sessionMock = mockk<Collaboration.Session>()
//            every { sessionMock.state } returns MutableStateFlow(Session.State.UnAuthenticated)
//            requester.setUp(sessionMock)
//            requester.dispose()
//        }
//    }
//
//    @Test
//    fun `show terms UI on terms agreement required session state`() = runTest {
//        ContextRetainer().create(mockk(relaxed = true))
//
//        val requester = TermsAndConditionsRequester(this@TermsAndConditionsRequesterTest::class.java, {}, {}, this)
//
//        mockkConstructor(TermsAndConditionsUI::class)
//        every { anyConstructed<TermsAndConditionsUI>().show() } returns Unit
//
//        val sessionMock = mockk<Collaboration.Session>()
//        every { sessionMock.state } returns MutableStateFlow(Session.State.Authenticating.TermsAgreementRequired(1, arrayOf(mockk(relaxed = true))))
//        requester.setUp(sessionMock)
//
//        advanceUntilIdle()
//        verify { anyConstructed<TermsAndConditionsUI>().show() }
//        coroutineContext.cancelChildren()
//    }
//
//    @Test
//    fun `setUp function dispose the previous session state job`() = runTest {
//        val requester = spyk(TermsAndConditionsRequester(this::class.java, {}, {}, backgroundScope))
//        val sessionMock = mockk<Collaboration.Session>()
//        every { sessionMock.state } returns MutableStateFlow(Session.State.UnAuthenticated)
//        requester.setUp(sessionMock)
//        verify { requester.dispose() }
//    }
}