/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.call.bottom_sheet.items

import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.buttons.BandyerActionButton
import com.bandyer.sdk_design.extensions.getCallActionItemStyle

/**
 * Class representing a Smartglass Call Action
 * @constructor
 */
sealed class SmartglassCallAction(@IdRes viewId: Int, @LayoutRes viewLayoutRes: Int = 0, @StyleRes viewStyle: Int = 0) : ActionItem(viewId, viewLayoutRes, viewStyle) {

    /**
     * Instance of Smartglass Call Actions
     */
    companion object Items {

        /**
         * Get all actions for an audio&Video call that can be collapsed
         * @param ctx Context
         * @param cameraToggled True if by default the camera should be toggled, false otherwise, null if not desired as action
         * @param micToggled True if by default the microphone should be toggled, false otherwise, null if not desired as action
         * @param withChat True if by default the chat should be shown, false otherwise
         * @param withWhiteboard  True if by default the whiteboard should be shown, false otherwise
         * @param withFileShare  True if by default the file share should be shown, false otherwise
         * @param withScreenShare  True if by default the screen share should be shown, false otherwise
         * @return List<CallAction>
         */
        fun getSmartglassActions(ctx: Context, micToggled: Boolean?, cameraToggled: Boolean?): List<SmartglassCallAction> =
                mutableListOf<SmartglassCallAction>().apply {
                    if (micToggled != null) add(SMARTGLASS_MICROPHONE(micToggled, ctx))
                    if (cameraToggled != null) add(SMARTGLASS_CAMERA(cameraToggled, ctx))
                    add(SMARTGLASS_PARTICIPANTS(ctx)) }
    }

    /**
     * Togglable Call Action
     * @property toggled true to select, false otherwise
     * @constructor
     */
    abstract class SmartglassTogglableCallAction(private val ctx: Context, viewId: Int, var toggled: Boolean, viewLayoutRes: Int = 0, viewStyle: Int = 0) : SmartglassCallAction(viewId, viewLayoutRes, viewStyle) {

        /**
         * Toggle the action button
         */
        fun toggle() {
            val actionButton = itemView?.findViewById<BandyerActionButton>(viewId) ?: return
            toggle(!actionButton.isActivated)
        }

        /**
         * Toggle the action button
         * @param enable true to enable, false otherwise
         */
        open fun toggle(enable: Boolean) {
            val actionButton = itemView?.findViewById<BandyerActionButton>(viewId) ?: return
            actionButton.isActivated = enable
            actionButton.label?.isActivated = enable
            toggled = enable
            updateContentDescription(actionButton.button)
        }

        /**
         * Update content description
         *
         * @param button view to update the content description
         */
        open fun updateContentDescription(button: View?) = Unit

        override fun onReady() {
            toggle(toggled)
        }
    }

    /**
     * Camera smartglass call action item
     * @property toggled true or false to toggle
     * @constructor
     */
    open class SMARTGLASS_CAMERA(mToggled: Boolean, private val ctx: Context) : SmartglassTogglableCallAction(ctx, R.id.bandyer_id_camera, mToggled, R.layout.bandyer_call_glass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_cameraStyle)) {

        override fun updateContentDescription(button: View?) {
            val text = if (toggled) ctx.resources.getString(R.string.bandyer_call_action_enable_camera_description)
            else ctx.resources.getString(R.string.bandyer_call_action_disable_camera_description)
            button?.contentDescription = text
        }

        override fun toggle(enable: Boolean) {
            super.toggle(enable)
            val actionButton = itemView?.findViewById<BandyerActionButton>(viewId) ?: return
            actionButton.label!!.text =
                    if (enable) ctx.resources.getString(R.string.bandyer_call_action_enable_camera_description)
                    else ctx.resources.getString(R.string.bandyer_call_action_disable_camera_description)
        }
    }

    /**
     * Microphone smartglass call action item
     * @property toggled true or false to toggle
     * @constructor
     */
    open class SMARTGLASS_MICROPHONE(mToggled: Boolean, private val ctx: Context) : SmartglassTogglableCallAction(ctx, R.id.bandyer_id_microphone, mToggled, R.layout.bandyer_call_glass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_microphoneStyle)) {

        override fun updateContentDescription(button: View?) {
            button?.contentDescription =
                    if (toggled) ctx.resources.getString(R.string.bandyer_call_action_enable_mic_description)
                    else ctx.resources.getString(R.string.bandyer_call_action_disable_mic_description)
        }

        override fun toggle(enable: Boolean) {
            super.toggle(enable)
            val actionButton = itemView?.findViewById<BandyerActionButton>(viewId) ?: return
            actionButton.label!!.text =
                    if (enable) ctx.resources.getString(R.string.bandyer_call_action_enable_mic_description)
                    else ctx.resources.getString(R.string.bandyer_call_action_disable_mic_description)
        }
    }

    /**
     * Participants smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_PARTICIPANTS(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_participants, R.layout.bandyer_call_glass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_participantsStyle))

    /**
     * Called when the layout has been inflated
     */
    override fun onReady() {}
}