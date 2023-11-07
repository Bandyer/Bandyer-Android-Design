package com.kaleyra.video_sdk.call.callactions.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Immutable
internal data class CallActionsUiState(
    val actionList: ImmutableList<CallAction> = ImmutableList(emptyList())
) : UiState