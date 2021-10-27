package com.bandyer.video_android_glass_ui.call

import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.bandyer.video_android_glass_ui.R
import com.google.android.material.textview.MaterialTextView

/**
 * Class representing a Menu Action
 * @property layoutRes layout to inflate
 * @constructor
 */
abstract class CallAction(@IdRes val viewId: Int, @LayoutRes val layoutRes: Int, @AttrRes val styleAttr: Int) {

    /**
     * Menu action item view
     */
    var itemView: View? = null

    /**
     * Instance of CallAction
     */
    companion object Items {
        /**
         * Get all actions for an audio&Video call
         * @param micToggled True if by default the microphone should be toggled, false otherwise, null if not desired as action
         * @param cameraToggled True if by default the camera should be toggled, false otherwise, null if not desired as action
         * @param withZoom True if by default the zoom should be shown, false otherwise
         * @param withParticipants True if by default the participants should be shown, false otherwise
         * @param withChat True if by default the chat should be shown, false otherwise
         * @return List<CallAction>
         */
        fun getActions(ctx: Context, micToggled: Boolean?, cameraToggled: Boolean?, withZoom: Boolean, withParticipants: Boolean, withChat: Boolean): List<CallAction> {
            return mutableListOf<CallAction>().apply {
                if(micToggled != null) add(MICROPHONE(micToggled, ctx))
                if(cameraToggled != null) add(CAMERA(cameraToggled, ctx))
                add(VOLUME())
                if (withZoom) add(ZOOM())
                if (withParticipants) add(PARTICIPANTS())
                if (withChat) add(CHAT())
            }
        }
    }

    /**
     * Method called when the layout has been inflated and bound
     */
    open fun onReady() = Unit

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CallAction) return false

        if (viewId != other.viewId) return false
        if (layoutRes != other.layoutRes) return false

        return true
    }

    override fun hashCode(): Int = viewId

    /**
     * Toggleable Menu Action
     * @property toggled true to activate, false otherwise
     * @constructor
     */
    abstract class ToggleableCallAction(private var toggled: Boolean, @IdRes viewId: Int, @LayoutRes layout: Int, @AttrRes styleAttr: Int): CallAction(viewId, layout, styleAttr) {
        /**
         * The text when the item is inactive
         */
        protected abstract val defaultText: String

        /**
         * The text when the item is active
         */
        protected abstract val toggledText: String

        /**
         * Toggle the menu action
         */
        fun toggle(toggled: Boolean) {
            itemView?.findViewById<MaterialTextView>(R.id.bandyer_text)?.apply {
                isActivated = toggled
                text = if(toggled) toggledText else defaultText
            }
            this.toggled = toggled
        }

        /**
         * @suppress
         */
        override fun onReady() {
            super.onReady()
            toggle(toggled)
        }
    }

    /**
     * Microphone menu action item
     * @property toggled true to activate, false otherwise
     * @constructor
     */
    class MICROPHONE(toggled: Boolean, ctx: Context): ToggleableCallAction(toggled, R.id.id_glass_menu_mic_item, R.layout.bandyer_glass_menu_item_layout, R.attr.bandyer_recyclerViewMicItemStyle) {
        override val defaultText = ctx.getString(R.string.bandyer_glass_menu_microphone)
        override val toggledText = ctx.getString(R.string.bandyer_glass_menu_microphone_toggled)
    }

    /**
     * Camera menu action item
     * @property toggled true to activate, false otherwise
     * @constructor
     */
    class CAMERA(toggled: Boolean, ctx: Context): ToggleableCallAction(toggled, R.id.id_glass_menu_camera_item, R.layout.bandyer_glass_menu_item_layout, R.attr.bandyer_recyclerViewCameraItemStyle) {
        override val defaultText = ctx.getString(R.string.bandyer_glass_menu_camera)
        override val toggledText = ctx.getString(R.string.bandyer_glass_menu_camera_toggled)
    }

    /**
     * Volume menu action item
     * @constructor
     */
    class VOLUME: CallAction(R.id.id_glass_menu_volume_item, R.layout.bandyer_glass_menu_item_layout, R.attr.bandyer_recyclerViewVolumeItemStyle)

    /**
     * Zoom menu action item
     * @constructor
     */
    class ZOOM: CallAction(R.id.id_glass_menu_zoom_item, R.layout.bandyer_glass_menu_item_layout, R.attr.bandyer_recyclerViewZoomItemStyle)

    /**
     * Participants menu action item
     * @constructor
     */
    class PARTICIPANTS: CallAction(R.id.id_glass_menu_participants_item, R.layout.bandyer_glass_menu_item_layout, R.attr.bandyer_recyclerViewParticipantsItemStyle)

    /**
     * Chat menu action item
     * @constructor
     */
    class CHAT: CallAction(R.id.id_glass_menu_chat_item, R.layout.bandyer_glass_menu_item_layout, R.attr.bandyer_recyclerViewChatItemStyle)
}
