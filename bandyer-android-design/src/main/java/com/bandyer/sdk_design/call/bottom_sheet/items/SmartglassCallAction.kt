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

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.view.AudioRouteState
import com.bandyer.sdk_design.buttons.AudioRouteButton
import com.bandyer.sdk_design.buttons.BandyerActionButton
import com.bandyer.sdk_design.call.buttons.BandyerAudioRouteActionButton
import com.bandyer.sdk_design.extensions.getCallActionItemStyle
import com.bandyer.sdk_design.extensions.getRingingActionItemStyle
import com.bandyer.sdk_design.extensions.setAllEnabled
import com.bandyer.sdk_design.extensions.setTextAppearance

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
        fun getSmartglassActions(ctx: Context, micToggled: Boolean?, cameraToggled: Boolean?, withChat: Boolean, withWhiteboard: Boolean, withFileShare: Boolean, withScreenShare: Boolean): List<SmartglassCallAction> =
                mutableListOf<SmartglassCallAction>().apply {
                    if (micToggled != null) add(SMARTGLASS_MICROPHONE(micToggled, ctx))
                    if (cameraToggled != null) add(SMARTGLASS_CAMERA(cameraToggled, ctx))
                    add(SMARTGLASS_AUDIOROUTE(ctx))
                    add(SMARTGLASS_HANGUP(ctx))
                }
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
    open class SMARTGLASS_CAMERA(mToggled: Boolean, private val ctx: Context) : SmartglassTogglableCallAction(ctx, R.id.bandyer_id_camera, mToggled, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_cameraStyle)) {

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
    open class SMARTGLASS_MICROPHONE(mToggled: Boolean, private val ctx: Context) : SmartglassTogglableCallAction(ctx, R.id.bandyer_id_microphone, mToggled, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_microphoneStyle)) {

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
     * Options smartglass call action item
     * @property switchWith CallAction to replace the options with
     * @constructor
     */
    open class SMARTGLASS_OPTIONS(val switchWith: CallAction, ctx: Context) : SmartglassCallAction(R.id.bandyer_id_options, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_optionsStyle))

    /**
     * Switch camera smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_SWITCH_CAMERA(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_switchcamera, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_switchCameraStyle))

    /**
     * Participants smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_PARTICIPANTS(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_participants, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_participantsStyle))

    /**
     * Open Chat smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_CHAT(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_chat, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_chatStyle))

    /**
     * AudioRoute smartglass call action item
     * @property mCurrent AudioRoute? current AudioRoute device
     * @constructor
     */
    open class SMARTGLASS_AUDIOROUTE(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_audioroute, R.layout.bandyer_call_smartglass_action_audioroute_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_audioRouteStyle)) {

        var mCurrent: AudioRoute? = null

        /**
         * Change current AudioRoute device
         * @param item new AudioRoute?
         */
        fun setCurrent(item: AudioRoute?) {
            if (item == null)
                return

            val view = itemView?.findViewById<BandyerAudioRouteActionButton>(viewId)
            val button = view?.button as? AudioRouteButton
            view?.post {
                when (item) {
                    is AudioRoute.BLUETOOTH -> {
                        button?.state = AudioRouteButton.State.BLUETOOTH
                    }
                    is AudioRoute.WIRED_HEADSET -> {
                        button?.state = AudioRouteButton.State.WIRED_HEADSET
                    }
                    is AudioRoute.LOUDSPEAKER -> {
                        button?.state = AudioRouteButton.State.LOUDSPEAKER
                    }
                    is AudioRoute.EARPIECE -> {
                        button?.state = AudioRouteButton.State.EARPIECE
                    }
                    is AudioRoute.MUTED -> {
                        button?.state = AudioRouteButton.State.MUTED
                    }
                }
            }
            view?.label?.text = item.name
            mCurrent = item
        }

        override fun onReady() {
            setCurrent(mCurrent)
        }

        override fun equals(other: Any?): Boolean {
            other ?: return false
            if (other !is SMARTGLASS_AUDIOROUTE) return false
            return this.mCurrent != mCurrent
        }

    }

    /**
     * Hangup smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_DECLINE(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_decline, R.layout.bandyer_call_smartglass_action_item, ctx.getRingingActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Ringing_bandyer_declineStyle))

    /**

     * Hangup smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_HANGUP(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_hangup, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_hangUpStyle))

    /**
     * Answer smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_ANSWER(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_answer, R.layout.bandyer_call_smartglass_action_item, ctx.getRingingActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Ringing_bandyer_answerStyle))


    /**
     * Whiteboard smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_WHITEBOARD(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_whiteboard, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_whiteboardStyle))


    /**
     * Upload smartglass call action item
     * @constructor
     */
    open class SMARTGLASS_FILE_SHARE(ctx: Context) : SmartglassCallAction(R.id.bandyer_id_fileshare, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_fileShareStyle)) {

        private var oldLabelText: String? = null

        /**
         * If action button is enabled
         */
        var enabled = true
            private set

        override fun onReady() {
            val action = itemView!!.findViewById<BandyerActionButton>(viewId)
            if (oldLabelText == null) oldLabelText = action.label?.text?.toString()
            action.label?.text = oldLabelText
            action.setAllEnabled(enabled)
        }

        /**
         * Set the Upload progress
         * @param progress percentage from 0 to 100
         */
        @SuppressLint("SetTextI18n")
        fun setUploadProgress(progress: Float) {
            itemView ?: return
            val action = itemView!!.findViewById<BandyerActionButton>(viewId)
            if (progress < 100) {
                action.label?.text = "${progress.toInt()}%"
            } else {
                action.label?.text = oldLabelText
            }
        }

        /**
         * Remove the progress
         */
        fun removeUploadProgress() {
            itemView ?: return
            val action = itemView!!.findViewById<BandyerActionButton>(viewId)
            action.label?.text = oldLabelText
        }

        /**
         * Enable the action
         */
        fun enable() {
            itemView ?: return
            enabled = true
            val action = itemView!!.findViewById<BandyerActionButton>(viewId)
            action.setAllEnabled(true)
        }

        /**
         * Disable the action
         */
        fun disable() {
            itemView ?: return
            enabled = false
            val action = itemView!!.findViewById<BandyerActionButton>(viewId)
            action.setAllEnabled(false)
        }
    }

    /**
     * Microphone smartglass call action item
     * @property toggled true or false to toggle
     * @constructor
     */
    open class SMARTGLASS_SCREEN_SHARE(mToggled: Boolean = false, ctx: Context) : SmartglassTogglableCallAction(ctx, R.id.bandyer_id_screenshare, mToggled, R.layout.bandyer_call_smartglass_action_item, ctx.getCallActionItemStyle(R.styleable.BandyerSDKDesign_BottomSheet_Call_bandyer_smartglass_screenShareStyle))

    /**
     * Called when the layout has been inflated
     */
    override fun onReady() {}
}