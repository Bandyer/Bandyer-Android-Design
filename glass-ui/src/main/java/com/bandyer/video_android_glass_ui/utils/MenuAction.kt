package com.bandyer.video_android_glass_ui.utils

import android.view.View
import androidx.annotation.LayoutRes
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassMenuItemLayoutBinding

/**
 * Class representing a Menu Action
 * @property layout layout to inflate
 * @constructor
 */
abstract class MenuAction(@LayoutRes val layout: Int) {

    /**
     * Menu action item view
     */
    var itemView: View? = null

    /**
     * Instance of MenuAction
     */
    companion object Items {
        /**
         * Get all actions for an audio&Video call
         * @param micToggled True if by default the microphone should be toggled, false otherwise, null if not desired as action
         * @param cameraToggled True if by default the camera should be toggled, false otherwise, null if not desired as action
         * @param withZoom True if by default the zoom should be shown, false otherwise
         * @param withParticipants True if by default the participants should be shown, false otherwise
         * @param withChat True if by default the chat should be shown, false otherwise
         * @return List<MenuAction>
         */
        fun getActions(micToggled: Boolean?, cameraToggled: Boolean?, withZoom: Boolean, withParticipants: Boolean, withChat: Boolean): List<MenuAction> {
            return mutableListOf<MenuAction>().apply {
                if(micToggled != null) add(MICROPHONE(micToggled))
                if(cameraToggled != null) add(CAMERA(cameraToggled))
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

    /**
     * Toggleable Menu Action
     * @property toggled true to activate, false otherwise
     * @constructor
     */
    abstract class ToggleableMenuAction(private var toggled: Boolean, @LayoutRes layout: Int): MenuAction(layout) {
        abstract val inactiveText: String
        abstract val activeText: String

        protected val binding: BandyerGlassMenuItemLayoutBinding = BandyerGlassMenuItemLayoutBinding.bind(itemView!!)

        /**
         * Toggle the menu action
         * @param active true to activate, false otherwise
         */
        open fun toggle(active: Boolean) = with(binding.bandyerText) {
            isActivated = active
            text = if(active) activeText else inactiveText
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
    class MICROPHONE(toggled: Boolean): ToggleableMenuAction(toggled, R.layout.bandyer_glass_menu_item_layout) {
        override val inactiveText = itemView!!.context.getString(R.string.bandyer_glass_menu_microphone)
        override val activeText = itemView!!.context.getString(R.string.bandyer_glass_menu_microphone_active)
    }

    /**
     * Camera menu action item
     * @property toggled true to activate, false otherwise
     * @constructor
     */
    class CAMERA(toggled: Boolean): ToggleableMenuAction(toggled, R.layout.bandyer_glass_menu_item_layout) {
        override val inactiveText = itemView!!.context.getString(R.string.bandyer_glass_menu_camera)
        override val activeText = itemView!!.context.getString(R.string.bandyer_glass_menu_camera_active)
    }

    /**
     * Volume menu action item
     * @constructor
     */
    class VOLUME: MenuAction(R.layout.bandyer_glass_menu_item_layout)

    /**
     * Zoom menu action item
     * @constructor
     */
    class ZOOM: MenuAction(R.layout.bandyer_glass_menu_item_layout)

    /**
     * Participants menu action item
     * @constructor
     */
    class PARTICIPANTS: MenuAction(R.layout.bandyer_glass_menu_item_layout)

    /**
     * Chat menu action item
     * @constructor
     */
    class CHAT: MenuAction(R.layout.bandyer_glass_menu_item_layout)
}
