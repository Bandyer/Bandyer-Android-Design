package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import android.content.Context
import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite_networking.Session
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TermsAndConditionsRequesterTest {

    private val contextMock = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns contextMock
    }

    @After
    fun teardDown() {
        unmockkAll()
    }

    @Test
    fun `session state job is cancelled on session authenticated state`() = runTest {
        withTimeout(100) {
            val requester = TermsAndConditionsRequester(this::class.java, {}, {}, this)
            val sessionMock = mockk<Collaboration.Session>()
            every { sessionMock.state } returns MutableStateFlow(Session.State.Authenticated)
            requester.setUp(sessionMock)
        }
    }

    @Test
    fun `dispose cancels session state job`() = runTest {
        withTimeout(100) {
            val requester = TermsAndConditionsRequester(this::class.java, {}, {}, this)
            val sessionMock = mockk<Collaboration.Session>()
            every { sessionMock.state } returns MutableStateFlow(Session.State.UnAuthenticated)
            requester.setUp(sessionMock)
            requester.dispose()
        }
    }

    @Test
    fun `setUp function dispose the previous session state job`() = runTest {
        val requester = spyk(TermsAndConditionsRequester(this::class.java, {}, {}, backgroundScope))
        val sessionMock = mockk<Collaboration.Session>()
        every { sessionMock.state } returns MutableStateFlow(Session.State.UnAuthenticated)
        requester.setUp(sessionMock)
        verify { requester.dispose() }
    }

    @Test
    fun `show terms UI on terms agreement required session state`() = runTest {
        val requester = TermsAndConditionsRequester(this@TermsAndConditionsRequesterTest::class.java, {}, {}, this)
        val sessionMock = mockk<Collaboration.Session>()

        mockkStatic(ContextRetainer::context){
            every { ContextRetainer.context.getString(any()) } returns "request terms"
        }

        mockkConstructor(TermsAndConditionsUI::class)
        every { anyConstructed<TermsAndConditionsUI>().show() } returns Unit
        every { sessionMock.state } returns MutableStateFlow(Session.State.Authenticating.TermsAgreementRequired(1, arrayOf(mockk(relaxed = true))))

        requester.setUp(sessionMock)

        advanceUntilIdle()
        verify { anyConstructed<TermsAndConditionsUI>().show() }
        coroutineContext.cancelChildren()
        unmockkStatic(ContextRetainer::context)
    }
}