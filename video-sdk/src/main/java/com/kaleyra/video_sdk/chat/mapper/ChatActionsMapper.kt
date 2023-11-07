package com.kaleyra.video_sdk.chat.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.ChatUI
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction

object ChatActionsMapper {
    fun Set<ChatUI.Action>.mapToChatActions(call: (Call.PreferredType) -> Unit): Set<ChatAction> {
        return mutableSetOf<ChatAction>().apply {
            val actions = this@mapToChatActions.filterIsInstance<ChatUI.Action.CreateCall>()
            actions.firstOrNull { !it.preferredType.hasVideo() }?.also { action ->
                add(ChatAction.AudioCall { call(action.preferredType) })
            }
            actions.firstOrNull { !it.preferredType.isVideoEnabled() }?.also { action ->
                add(ChatAction.AudioUpgradableCall { call(action.preferredType) })
            }
            actions.firstOrNull { it.preferredType.isVideoEnabled() }?.also { action ->
                add(ChatAction.VideoCall { call(action.preferredType) })
            }
        }
    }
}