package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.isConnected
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallStateMapper.toCallStateUi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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

    private val callParticipantsMock = mockk<CallParticipants> {
        every { me } returns participantMeMock
    }

    @Before
    fun setUp() {
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
    }

    @Test
    fun stateConnected_toCallStateUi_callStateConnected() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Connected, result.first())
    }

    @Test
    fun stateReconnecting_toCallStateUi_callStateReconnecting() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Reconnecting)
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Reconnecting, result.first())
    }

    @Test
    fun stateConnectingAndIAmCallCreator_toCallStateUi_callStateDialing() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns participantMeMock
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Dialing, result.first())
    }

    @Test
    fun stateConnectingAndIAmNotCallCreator_toCallStateUi_callStateConnecting() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns mockk()
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Connecting, result.first())
    }

    @Test
    fun stateAnsweredOnAnotherDevice_toCallStateUi_callStateAnsweredOnAnotherDevice() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.AnsweredOnAnotherDevice, result.first())
    }

    @Test
    fun stateDeclined_toCallStateUi_callStateDeclined() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Declined)
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.Declined, result.first())
    }

    @Test
    fun stateLineBusy_toCallStateUi_callStateLineBusy() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.LineBusy)
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.LineBusy, result.first())
    }

    @Test
    fun stateTimeout_toCallStateUi_callStateTimeout() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Timeout)
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.Timeout, result.first())
    }

    @Test
    fun stateServerError_toCallStateUi_callStateErrorServer() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Server())
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.Error.Server, result.first())
    }

    @Test
    fun stateUnknownError_toCallStateUi_callStateUnknownError() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Unknown())
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.Error.Unknown, result.first())
    }

    @Test
    fun stateHungUp_toCallStateUi_callStateHungUp() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.HungUp())
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.HungUp, result.first())
    }

    @Test
    fun stateKicked_toCallStateUi_callStateKicked() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked("userId"))
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.Kicked("userId"), result.first())
    }

    @Test
    fun stateError_toCallStateUi_callStateError() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error)
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected.Ended.Error, result.first())
    }

    @Test
    fun stateDisconnectedAndIAmNotCallCreator_toCallStateUi_callStateRinging() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns mockk()
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Ringing, result.first())
    }

    @Test
    fun stateDisconnected_toCallStateUi_callStateDisconnected() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns participantMeMock
        val result = MutableStateFlow(callMock).toCallStateUi()
        Assert.assertEquals(CallState.Disconnected, result.first())
    }

    @Test
    fun stateConnected_isConnected_true() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(true, actual)
    }


    @Test
    fun stateUnknownError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Unknown())
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateServerError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Server())
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateTimeout_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Timeout)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateLineBusy_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.LineBusy)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateAnsweredOnAnotherDevice_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateKicked_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked(""))
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateDeclined_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Declined)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateHungUp_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.HungUp())
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateEnded_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateDisconnected_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateConnecting_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateReconnecting_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Reconnecting)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        Assert.assertEquals(false, actual)
    }
}