/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *           
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.buttons.AudioRouteButton
import com.kaleyra.collaboration_suite_phone_ui.buttons.KaleyraActionButton
import com.kaleyra.collaboration_suite_phone_ui.call.buttons.KaleyraAudioRouteActionButton
import com.kaleyra.collaboration_suite_phone_ui.extensions.getCallActionItemStyle
import com.kaleyra.collaboration_suite_phone_ui.extensions.getRingingActionItemStyle
import com.kaleyra.collaboration_suite_phone_ui.extensions.setAllEnabled

/**
 * Class representing a Call Action
 * @constructor
 */
open class CallAction(@IdRes viewId: Int, @LayoutRes viewLayoutRes: Int = 0, @StyleRes viewStyle: Int = 0) : ActionItem(viewId, viewLayoutRes, viewStyle) {

    /**
     * If action button is enabled
     */
    var isEnabled = true
        set(value) {
            field = value
            itemView?.findViewById<KaleyraActionButton>(viewId)?.setAllEnabled(value)
        }

    /**
     * Instance of Call Actions
     */
    companion object Items {

        /**
         * Gives a list of all actions used for incoming calls
         * @param ctx Context input context
         * @return List<CallAction> composed of BIG_HANGUP and BIG_ANSWER
         */
        fun getIncomingCallActions(ctx: Context) = listOf(DECLINE(ctx), ANSWER(ctx))

        /**
         * Get all actions for an audio&Video call that can be collapsed
         * @param ctx Context
         * @param cameraToggled True if by default the camera should be toggled, false otherwise, null if not desired as action
         * @param micToggled True if by default the microphone should be toggled, false otherwise, null if not desired as action
         * @param withChat True if by default the chat should be shown, false otherwise
         * @param withWhiteboard  True if by default the whiteboard should be shown, false otherwise
         * @param withFileShare  True if by default the file share should be shown, false otherwise
         * @param withScreenShare  True if by default the screen share should be shown, false otherwise
         * @param withVirtualBackground  True if by default the virtual background button should be shown, false otherwise
         * @return List<CallAction>
         */
        fun getActions(ctx: Context, micToggled: Boolean?, cameraToggled: Boolean?, withChat: Boolean, withWhiteboard: Boolean, withFileShare: Boolean, withScreenShare: Boolean, withVirtualBackground: Boolean): List<CallAction> =
            mutableListOf<CallAction>().apply {
                if (micToggled != null) add(MICROPHONE(micToggled, ctx))
                if (cameraToggled != null) {
                    add(CAMERA(cameraToggled, ctx))
                    add(SWITCH_CAMERA(ctx))
                }
                if (withChat) add(CHAT(ctx))
                if (withWhiteboard) add(WHITEBOARD(ctx))
                add(AUDIOROUTE(ctx))
                if (withFileShare) add(FILE_SHARE(ctx))
                if (withScreenShare) add(SCREEN_SHARE(false, ctx))
                if (withVirtualBackground) add(VIRTUAL_BACKGROUND(false, ctx))
                if (size < 3) add(1, HANGUP(ctx))
                else add(3, HANGUP(ctx))
            }
    }

    /**
     * Togglable Call Action
     * @property toggled true to select, false otherwise
     * @constructor
     */
    abstract class TogglableCallAction(viewId: Int, var toggled: Boolean, viewLayoutRes: Int = 0, viewStyle: Int = 0) : CallAction(viewId, viewLayoutRes, viewStyle) {

        /**
         * Toggle the action button
         */
        fun toggle() {
            val actionButton = itemView?.findViewById<KaleyraActionButton>(viewId) ?: return
            toggle(!actionButton.isActivated)
        }

        /**
         * Toggle the action button
         * @param enable true to enable, false otherwise
         */
        open fun toggle(enable: Boolean) {
            val actionButton = itemView?.findViewById<KaleyraActionButton>(viewId) ?: return
            actionButton.isActivated = enable
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
            super.onReady()
            toggle(toggled)
        }
    }

    /**
     * Camera call action item
     * @property toggled true or false to toggle
     * @constructor
     */
    open class CAMERA(mToggled: Boolean, private val ctx: Context) : TogglableCallAction(R.id.kaleyra_id_camera, mToggled, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_cameraStyle)) {

        override fun updateContentDescription(button: View?) {
            button?.contentDescription =
                if (toggled) ctx.resources.getString(R.string.kaleyra_call_action_enable_camera_description)
                else ctx.resources.getString(R.string.kaleyra_call_action_disable_camera_description)
        }
    }

    /**
     * Microphone call action item
     * @property toggled true or false to toggle
     * @constructor
     */
    open class MICROPHONE(mToggled: Boolean, private val ctx: Context) : TogglableCallAction(R.id.kaleyra_id_microphone, mToggled, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_microphoneStyle)) {

        override fun updateContentDescription(button: View?) {
            button?.contentDescription =
                if (toggled) ctx.resources.getString(R.string.kaleyra_call_action_enable_mic_description)
                else ctx.resources.getString(R.string.kaleyra_call_action_disable_mic_description)
        }
    }

    /**
     * Options call action item
     * @property switchWith CallAction to replace the options with
     * @constructor
     */
    open class OPTIONS(val switchWith: CallAction, ctx: Context) : CallAction(R.id.kaleyra_id_options, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_optionsStyle))

    /**
     * Switch camera call action item
     * @constructor
     */
    open class SWITCH_CAMERA(ctx: Context) : CallAction(R.id.kaleyra_id_switchcamera, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_switchCameraStyle))

    /**
     * Participants call action item
     * @constructor
     */
    open class PARTICIPANTS(ctx: Context) : CallAction(R.id.kaleyra_id_participants, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_participantsStyle))

    /**
     * Open Chat call action item
     * @constructor
     */
    open class CHAT(ctx: Context) : CallAction(R.id.kaleyra_id_chat, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_chatStyle))

    /**
     * AudioRoute call action item
     * @property mCurrent AudioRoute? current AudioRoute device
     * @constructor
     */
    open class AUDIOROUTE(ctx: Context) : CallAction(R.id.kaleyra_id_audioroute, R.layout.kaleyra_call_action_audioroute_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_audioRouteStyle)) {

        var mCurrent: AudioRoute? = null

        /**
         * Change current AudioRoute device
         * @param item new AudioRoute?
         */
        fun setCurrent(item: AudioRoute?) {
            if (item == null)
                return

            val view = itemView?.findViewById<KaleyraAudioRouteActionButton>(viewId)
            val button = view?.button as? AudioRouteButton
            view?.post {
                when (item) {
                    is AudioRoute.BLUETOOTH     -> {
                        button?.state = AudioRouteButton.State.BLUETOOTH
                    }
                    is AudioRoute.WIRED_HEADSET -> {
                        button?.state = AudioRouteButton.State.WIRED_HEADSET
                    }
                    is AudioRoute.LOUDSPEAKER   -> {
                        button?.state = AudioRouteButton.State.LOUDSPEAKER
                    }
                    is AudioRoute.EARPIECE      -> {
                        button?.state = AudioRouteButton.State.EARPIECE
                    }
                    is AudioRoute.MUTED         -> {
                        button?.state = AudioRouteButton.State.MUTED
                    }
                }
            }
            mCurrent = item
        }

        override fun onReady() {
            super.onReady()
            setCurrent(mCurrent)
        }

    }

    /**
     * Hangup call action item
     * @constructor
     */
    open class DECLINE(ctx: Context) : CallAction(R.id.kaleyra_id_decline, R.layout.kaleyra_call_action_item, ctx.getRingingActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Ringing_kaleyra_declineStyle))

    /**

     * Hangup call action item
     * @constructor
     */
    open class HANGUP(ctx: Context) : CallAction(R.id.kaleyra_id_hangup, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_hangUpStyle))

    /**
     * Answer call action item
     * @constructor
     */
    open class ANSWER(ctx: Context) : CallAction(R.id.kaleyra_id_answer, R.layout.kaleyra_call_action_item, ctx.getRingingActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Ringing_kaleyra_answerStyle))


    /**
     * Whiteboard call action item
     * @constructor
     */
    open class WHITEBOARD(ctx: Context) : CallAction(R.id.kaleyra_id_whiteboard, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_whiteboardStyle))


    /**
     * Upload call action item
     * @constructor
     */
    open class FILE_SHARE(ctx: Context) : CallAction(R.id.kaleyra_id_fileshare, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_fileShareStyle)) {

        private var oldLabelText: String? = null

        override fun onReady() {
            super.onReady()
            val action = itemView!!.findViewById<KaleyraActionButton>(viewId)
            if (oldLabelText == null) oldLabelText = action.label?.text?.toString()
            action.label?.text = oldLabelText
        }

        /**
         * Set the Upload progress
         * @param progress percentage from 0 to 100
         */
        @SuppressLint("SetTextI18n")
        fun setUploadProgress(progress: Float) {
            itemView ?: return
            val action = itemView!!.findViewById<KaleyraActionButton>(viewId)
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
            val action = itemView!!.findViewById<KaleyraActionButton>(viewId)
            action.label?.text = oldLabelText
        }
    }

    /**
     * Microphone call action item
     * @property toggled true or false to toggle
     * @constructor
     */
    open class SCREEN_SHARE(mToggled: Boolean = false, ctx: Context) : TogglableCallAction(R.id.kaleyra_id_screenshare, mToggled, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_screenShareStyle))


    /**
     * Virtual background call action item
     * @property toggled true or false to toggle
     * @constructor
     */
    open class VIRTUAL_BACKGROUND(mToggled: Boolean = false, ctx: Context) : TogglableCallAction(R.id.kaleyra_id_virtual_background, mToggled, R.layout.kaleyra_call_action_item, ctx.getCallActionItemStyle(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call_kaleyra_virtualBackgroundStyle))

    /**
     * Called when the layout has been inflated
     */
    override fun onReady() {
        if (itemView?.isEnabled == isEnabled) return
        itemView?.setAllEnabled(isEnabled)
    }
}