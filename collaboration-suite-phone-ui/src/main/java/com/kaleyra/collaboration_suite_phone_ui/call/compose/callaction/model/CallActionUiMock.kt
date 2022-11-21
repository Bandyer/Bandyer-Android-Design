package com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model

import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

val mockCallActions = ImmutableList(
    listOf(
        CallAction.Microphone(isToggled = false, isEnabled = true) {},
        CallAction.Camera(isToggled = false, isEnabled = false) {},
        CallAction.SwitchCamera(true) {},
        CallAction.HangUp(true) {},
        CallAction.Chat(true) {},
        CallAction.Whiteboard(true) {},
        CallAction.Audio(true) {},
        CallAction.FileShare(true) {},
        CallAction.ScreenShare(true) {}
    )
)