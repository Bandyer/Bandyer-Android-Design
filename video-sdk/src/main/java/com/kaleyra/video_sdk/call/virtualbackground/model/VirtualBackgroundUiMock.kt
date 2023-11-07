package com.kaleyra.video_sdk.call.virtualbackground.model

import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

val mockVirtualBackgrounds = ImmutableList(listOf(
    VirtualBackgroundUi.None,
    VirtualBackgroundUi.Blur("id"),
    VirtualBackgroundUi.Image("id2")
))