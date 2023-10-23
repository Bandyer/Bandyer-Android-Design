package com.kaleyra.collaboration_suite_core_ui.mapper

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Input
import com.kaleyra.collaboration_suite_core_ui.mapper.InputMapper.isAnyScreenInputActive
import com.kaleyra.collaboration_suite_core_ui.mapper.InputMapper.isAppScreenInputActive
import com.kaleyra.collaboration_suite_core_ui.mapper.InputMapper.isDeviceScreenInputActive
import com.kaleyra.collaboration_suite_core_ui.mapper.InputMapper.isInputActive
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class InputMapperTests {

    private val activeMicrophoneInputMock = mockk<Input.Audio> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveMicrophoneInputMock = mockk<Input.Audio> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }
    private val activeScreenInputMock = mockk<Input.Video.Screen> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveScreenInputMock = mockk<Input.Video.Screen> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }
    private val activeApplicationInputMock = mockk<Input.Video.Application> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveApplicationInputMock = mockk<Input.Video.Application> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }
    private val callMock = mockk<Call>()
    private val callFlow: Flow<Call> = flowOf(callMock)

    @Test
    fun noInput_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())
        Assert.assertEquals(false, callFlow.isInputActive<Input.Video.Camera>().first())
    }

    @Test
    fun wrongInput_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeMicrophoneInputMock))
        Assert.assertEquals(false, callFlow.isInputActive<Input.Video.Camera>().first())
    }

    @Test
    fun expectedInput_isInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeMicrophoneInputMock))
        Assert.assertEquals(true, callFlow.isInputActive<Input.Audio>().first())
    }

    @Test
    fun expectedInputNotActive_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveMicrophoneInputMock))
        Assert.assertEquals(false, callFlow.isInputActive<Input.Audio>().first())
    }

    @Test
    fun deviceScreenActive_isDeviceScreenInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock))
        Assert.assertEquals(true, callFlow.isDeviceScreenInputActive().first())
    }

    @Test
    fun deviceScreenNotActive_isDeviceScreenInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveScreenInputMock))
        Assert.assertEquals(false, callFlow.isDeviceScreenInputActive().first())
    }

    @Test
    fun applicationScreenActive_isDeviceApplicationInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeApplicationInputMock))
        Assert.assertEquals(true, callFlow.isAppScreenInputActive().first())
    }

    @Test
    fun applicationScreenNotActive_isDeviceApplicationInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveApplicationInputMock))
        Assert.assertEquals(false, callFlow.isAppScreenInputActive().first())
    }

    @Test
    fun deviceScreenActive_isAnyScreenInputActive_true()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock))
        Assert.assertEquals(true, callFlow.isAnyScreenInputActive().first())
    }

    @Test
    fun allDeviceActive_isAnyScreenInputActive_true()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock, activeApplicationInputMock))
        Assert.assertEquals(true, callFlow.isAnyScreenInputActive().first())
    }

    @Test
    fun allDeviceNotActive_isAnyScreenInputActive_false()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveScreenInputMock, inactiveApplicationInputMock))
        Assert.assertEquals(false, callFlow.isAnyScreenInputActive().first())
    }
}