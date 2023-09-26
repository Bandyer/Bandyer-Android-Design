package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatAction

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