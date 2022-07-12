package com.kaleyra.collaboration_suite_glass_ui.chat

import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassMenuItemLayoutBinding

/**
 * Class representing a Chat Menu Action
 * @property layoutRes layout to inflate
 * @constructor
 */
internal abstract class ChatAction(
    @IdRes val viewId: Int,
    @LayoutRes val layoutRes: Int,
    @AttrRes val styleAttr: Int
) {

    /**
     * Instance of ChatAction
     */
    companion object Items {
        /**
         * Get all actions for an audio&Video call
         * @param withVideoCall True to add the video call action, false otherwise
         * @param withCall True to add the call action, false otherwise
         *
         * @return List<ChatAction>
         */
        fun getActions(
            withVideoCall: Boolean,
            withCall: Boolean,
        ): List<ChatAction> = mutableListOf<ChatAction>().apply {
            if (withVideoCall) add(VIDEOCALL())
            if (withCall) add(CALL())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChatAction) return false

        if (viewId != other.viewId) return false
        if (layoutRes != other.layoutRes) return false

        return true
    }

    override fun hashCode(): Int = viewId

    /**
     * Switch camera menu action item
     * @constructor
     */
    class VIDEOCALL : ChatAction(
        R.id.id_glass_chat_menu_videocall_item,
        R.layout.kaleyra_glass_chat_menu_item_layout,
        R.attr.kaleyra_recyclerViewVideoCallItemStyle
    )

    /**
     * Zoom menu action item
     * @constructor
     */
    class CALL : ChatAction(
        R.id.id_glass_chat_menu_call_item,
        R.layout.kaleyra_glass_chat_menu_item_layout,
        R.attr.kaleyra_recyclerViewCallItemStyle
    )
}