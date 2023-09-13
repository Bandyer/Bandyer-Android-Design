package com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.model

import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

val mockVirtualBackgrounds = ImmutableList(listOf(
    VirtualBackgroundUi.None,
    VirtualBackgroundUi.Blur("id"),
    VirtualBackgroundUi.Image("id2")
))