package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.isConnected
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CallStateMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val callParticipantsMock = mockk<CallParticipants>()

    private val callFlow = MutableStateFlow(callMock)

    @Before
    fun setUp() {
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callFlow.amIAlone() } returns flowOf(false)
        }
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        every { callParticipantsMock.me } returns participantMeMock
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun stateConnected_toCallStateUi_callStateConnected() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Connected, result.first())
    }

    @Test
    fun stateReconnecting_toCallStateUi_callStateReconnecting() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Reconnecting)
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Reconnecting, result.first())
    }

    @Test
    fun stateConnectingAndIAmCallCreator_toCallStateUi_callStateDialing() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns participantMeMock
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Dialing, result.first())
    }

    @Test
    fun `keep dialing state if I am alone`() = runTest {
        val stateFlow = MutableStateFlow<Call.State>(Call.State.Connecting)
        every { callMock.state } returns stateFlow
        every { callParticipantsMock.creator() } returns participantMeMock
        with(StreamMapper) {
            every { callFlow.amIAlone() } returns flowOf(true)
        }
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Dialing, result.first())
        stateFlow.value = Call.State.Connected
        Assert.assertEquals(CallStateUi.Dialing, result.first())
    }

    @Test
    fun stateConnectingAndIAmNotCallCreator_toCallStateUi_callStateRinging() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns mockk()
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Ringing(true), result.first())
    }

    @Test
    fun stateAnsweredOnAnotherDevice_toCallStateUi_callStateAnsweredOnAnotherDevice() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice, result.first())
    }

    @Test
    fun stateDeclined_toCallStateUi_callStateDeclined() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Declined)
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Declined, result.first())
    }

    @Test
    fun stateLineBusy_toCallStateUi_callStateLineBusy() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.LineBusy)
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.LineBusy, result.first())
    }

    @Test
    fun stateTimeout_toCallStateUi_callStateTimeout() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Timeout)
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Timeout, result.first())
    }

    @Test
    fun stateServerError_toCallStateUi_callStateErrorServer() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Server())
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Error.Server, result.first())
    }

    @Test
    fun stateUnknownError_toCallStateUi_callStateUnknownError() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Unknown())
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Error.Unknown, result.first())
    }

    @Test
    fun stateHungUp_toCallStateUi_callStateHungUp() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.HungUp())
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.HungUp, result.first())
    }

    @Test
    fun stateKicked_toCallStateUi_callStateKicked() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked("userId"))
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Kicked("userId"), result.first())
    }

    @Test
    fun stateError_toCallStateUi_callStateError() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error)
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Error, result.first())
    }

    @Test
    fun stateDisconnectedAndIAmNotCallCreator_toCallStateUi_callStateRinging() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns mockk()
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Ringing(false), result.first())
    }

    @Test
    fun stateDisconnectedEndedAndIAmNotCallCreator_toCallStateUi_callStateEnded() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { callParticipantsMock.creator() } returns mockk()
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended, result.first())
    }

    @Test
    fun stateConnectedAndIAmNotCallCreatorAndIAmAlone_toCallStateUi_callStateRinging() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        every { callParticipantsMock.creator() } returns mockk()
        with(StreamMapper) {
            every { callFlow.amIAlone() } returns flowOf(true)
        }
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Ringing(true), result.first())
    }

    @Test
    fun stateConnectedAndIAmCallCreatorAndIAmAlone_toCallStateUi_callStateDialing() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        every { callParticipantsMock.creator() } returns participantMeMock
        with(StreamMapper) {
            every { callFlow.amIAlone() } returns flowOf(true)
        }
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Dialing, result.first())
    }

    @Test
    fun stateDisconnected_toCallStateUi_callStateDisconnected() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns participantMeMock
        val result = callFlow.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected, result.first())
    }

    @Test
    fun stateConnected_isConnected_true() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(true, actual)
    }


    @Test
    fun stateUnknownError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Unknown())
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateServerError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Server())
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateTimeout_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Timeout)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateLineBusy_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.LineBusy)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateAnsweredOnAnotherDevice_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateKicked_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked(""))
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateDeclined_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Declined)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateHungUp_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.HungUp())
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateEnded_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateDisconnected_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateConnecting_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateReconnecting_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Reconnecting)
        val actual = callFlow.isConnected().first()
        Assert.assertEquals(false, actual)
    }
}