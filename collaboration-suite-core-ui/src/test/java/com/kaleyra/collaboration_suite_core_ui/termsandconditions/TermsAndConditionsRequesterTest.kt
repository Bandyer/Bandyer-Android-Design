package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import com.kaleyra.collaboration_suite.State
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