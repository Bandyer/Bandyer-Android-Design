package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Effect
import com.kaleyra.collaboration_suite.phonebox.Effects
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper.toCurrentVirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VirtualBackgroundMapper.toVirtualBackgroundsUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
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

@OptIn(ExperimentalCoroutinesApi::class)
class VirtualBackgroundMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>()

    private val myStreamMock = mockk<Stream.Mutable>()

    private val myVideoMock = mockk<Input.Video.My>()

    private val effectsMock = mockk<Effects>()

    @Before
    fun setUp() {
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { participantsMock.me } returns meMock
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        with(myStreamMock) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(myVideoMock)
        }
        every { callMock.effects } returns effectsMock
    }

    @Test
    fun backgroundBlurEffect_toCurrentVirtualBackground_blurBackgroundUi() = runTest {
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur(id = "blurId", factor = 1f))
        val flow = MutableStateFlow(callMock)
        val actual = flow.toCurrentVirtualBackgroundUi().first()
        Assert.assertEquals(VirtualBackgroundUi.Blur("blurId"), actual)
    }

    @Test
    fun backgroundImageEffect_toCurrentVirtualBackground_imageBackgroundUi() = runTest {
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image(id = "imageId", image = mockk()))
        val flow = MutableStateFlow(callMock)
        val actual = flow.toCurrentVirtualBackgroundUi().first()
        Assert.assertEquals(VirtualBackgroundUi.Image("imageId"), actual)
    }

    @Test
    fun backgroundNoneEffect_toCurrentVirtualBackground_noneBackgroundUi() = runTest {
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.None)
        val flow = MutableStateFlow(callMock)
        val actual = flow.toCurrentVirtualBackgroundUi().first()
        Assert.assertEquals(VirtualBackgroundUi.None, actual)
    }

    @Test
    fun emptyAvailableVideoEffect_toVirtualBackgroundsUi_listHasOnlyNoneBackground() = runTest {
        every { effectsMock.available } returns MutableStateFlow(setOf())
        val flow = MutableStateFlow(callMock)
        val actual = flow.toVirtualBackgroundsUi().first()
        Assert.assertEquals(listOf<VirtualBackgroundUi>(VirtualBackgroundUi.None), actual)
    }

    @Test
    fun availableVideoEffectList_toVirtualBackgroundsUi_mappedVirtualBackgroundUiList() = runTest {
        every { effectsMock.available } returns MutableStateFlow(setOf(Effect.Video.Background.Blur(id = "blurId", factor = 1f), Effect.Video.Background.Image(id = "imageId", image = mockk())))
        val flow = MutableStateFlow(callMock)
        val actual = flow.toVirtualBackgroundsUi().first()
        Assert.assertEquals(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur(id = "blurId"), VirtualBackgroundUi.Image(id = "imageId")), actual)
    }
}