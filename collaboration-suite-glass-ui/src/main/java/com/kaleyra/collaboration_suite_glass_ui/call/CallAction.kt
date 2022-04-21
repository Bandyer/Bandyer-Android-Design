/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.call

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassMenuItemLayoutBinding

/**
 * Class representing a Menu Action
 * @property layoutRes layout to inflate
 * @constructor
 */
internal abstract class CallAction(
    @IdRes val viewId: Int,
    @LayoutRes val layoutRes: Int,
    @AttrRes val styleAttr: Int
) {

    /**
     * Menu action binding
     */
    var binding: KaleyraGlassMenuItemLayoutBinding? = null

    /**
     * Instance of CallAction
     */
    companion object Items {
        /**
         * Get all actions for an audio&Video call
         * @param withCamera True to add the camera action, false otherwise
         * @param withChat True to add the chat action, false otherwise
         * @return List<CallAction>
         */
        fun getActions(
            ctx: Context,
            withMicrophone: Boolean,
            withCamera: Boolean,
            withSwitchCamera: Boolean,
            withFlashLight: Boolean,
            withZoom: Boolean,
            withChat: Boolean
        ): List<CallAction> {
            return mutableListOf<CallAction>().apply {
                if (withMicrophone) add(MICROPHONE(ctx))
                if (withCamera) add(CAMERA(ctx))
                if (withSwitchCamera) add(SWITCHCAMERA())
                add(VOLUME())
                if (withZoom) add(ZOOM(ctx))
                if (withFlashLight) add(FLASHLIGHT(ctx))
                add(PARTICIPANTS())
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
     * @property isToggled true to activate, false otherwise
     * @constructor
     */
    abstract class ToggleableCallAction(
        @IdRes viewId: Int,
        @LayoutRes layout: Int,
        @AttrRes styleAttr: Int
    ) : CallAction(viewId, layout, styleAttr) {

        /**
         * The text when the item is inactive
         */
        protected abstract val defaultText: String

        /**
         * The text when the item is active
         */
        protected abstract val toggledText: String

        /**
         * Tells if the item is toggled
         */
        var isToggled: Boolean = true
            private set

        /**
         * Tells if the item is disabled
         */
        var isDisabled: Boolean = false
            private set

        /**
         * Toggle the menu action
         */
        fun toggle(toggled: Boolean) {
            binding?.kaleyraText?.apply {
                isActivated = !toggled
                text = if (toggled) toggledText else defaultText
            }
            this.isToggled = toggled
        }

        /**
         * Disable the menu action
         */
        fun disable(disabled: Boolean) {
            binding?.kaleyraText?.isEnabled = !disabled
            this.isDisabled = disabled
        }

        /**
         * @suppress
         */
        override fun onReady() {
            super.onReady()
            toggle(isToggled)
            disable(isDisabled)
        }
    }

    /**
     * Microphone menu action item
     * @property isToggled true to activate, false otherwise
     * @constructor
     */
    class MICROPHONE(ctx: Context) : ToggleableCallAction(
        R.id.id_glass_menu_mic_item,
        R.layout.kaleyra_glass_menu_item_layout,
        R.attr.kaleyra_recyclerViewMicItemStyle
    ) {
        override val defaultText = ctx.getString(R.string.kaleyra_glass_menu_microphone)
        override val toggledText = ctx.getString(R.string.kaleyra_glass_menu_microphone_toggled)
    }

    /**
     * Camera menu action item
     * @property isToggled true to activate, false otherwise
     * @constructor
     */
    class CAMERA(ctx: Context) : ToggleableCallAction(
        R.id.id_glass_menu_camera_item,
        R.layout.kaleyra_glass_menu_item_layout,
        R.attr.kaleyra_recyclerViewCameraItemStyle
    ) {
        override val defaultText = ctx.getString(R.string.kaleyra_glass_menu_camera)
        override val toggledText = ctx.getString(R.string.kaleyra_glass_menu_camera_toggled)
    }

    /**
     * Torch menu action item
     * @property isToggled true to activate, false otherwise
     * @constructor
     */
    class FLASHLIGHT(ctx: Context) : ToggleableCallAction(
        R.id.id_glass_menu_flashlight_item,
        R.layout.kaleyra_glass_menu_item_layout,
        R.attr.kaleyra_recyclerViewFlashLightItemStyle
    ) {
        override val defaultText = ctx.getString(R.string.kaleyra_glass_menu_flashlight)
        override val toggledText = ctx.getString(R.string.kaleyra_glass_menu_flashlight_toggled)
    }

    /**
     * Zoom menu action item
     * @constructor
     */
    class ZOOM(ctx: Context) : ToggleableCallAction(
        R.id.id_glass_menu_zoom_item,
        R.layout.kaleyra_glass_menu_item_layout,
        R.attr.kaleyra_recyclerViewZoomItemStyle
    ) {
        override val defaultText = ctx.getString(R.string.kaleyra_glass_menu_zoom)
        override val toggledText = ctx.getString(R.string.kaleyra_glass_menu_zoom)
    }

    /**
     * Switch camera menu action item
     * @constructor
     */
    class SWITCHCAMERA : CallAction(
        R.id.id_glass_menu_switch_camera_item,
        R.layout.kaleyra_glass_menu_item_layout,
        R.attr.kaleyra_recyclerViewSwitchCameraItemStyle
    )

    /**
     * Volume menu action item
     * @constructor
     */
    class VOLUME : CallAction(
        R.id.id_glass_menu_volume_item,
        R.layout.kaleyra_glass_menu_item_layout,
        R.attr.kaleyra_recyclerViewVolumeItemStyle
    )

    /**
     * Participants menu action item
     * @constructor
     */
    class PARTICIPANTS : CallAction(
        R.id.id_glass_menu_participants_item,
        R.layout.kaleyra_glass_menu_item_layout,
        R.attr.kaleyra_recyclerViewParticipantsItemStyle
    )

    /**
     * Chat menu action item
     * @constructor
     */
    class CHAT : CallAction(
        R.id.id_glass_menu_chat_item,
        R.layout.kaleyra_glass_menu_item_layout,
        R.attr.kaleyra_recyclerViewChatItemStyle
    )
}
