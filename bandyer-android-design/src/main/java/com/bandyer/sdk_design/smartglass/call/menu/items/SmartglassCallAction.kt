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

package com.bandyer.sdk_design.smartglass.call.menu.items

import android.content.Context
import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.buttons.BandyerActionButton
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.extensions.getSmartGlassCallActionItemStyle

/**
 * Get all available smart glass actions
 * @param ctx Context
 * @param cameraToggled True if by default the camera should be toggled, false otherwise, null if not desired as action
 * @param micToggled True if by default the microphone should be toggled, false otherwise, null if not desired as action
 * @return List<CallAction>
 */
fun CallAction.Items.getSmartglassActions(ctx: Context, micToggled: Boolean?, cameraToggled: Boolean?): List<CallAction> =
        mutableListOf<CallAction>().apply {
            if (micToggled != null) add(SMART_GLASS_MICROPHONE(micToggled, ctx))
            if (cameraToggled != null) add(SMART_GLASS_CAMERA(cameraToggled, ctx))
            add(SMART_GLASS_PARTICIPANTS(ctx))
        }

/**
 * Camera smart glass call action item
 * @property toggled true or false to toggle
 * @constructor
 */
open class SMART_GLASS_CAMERA(mToggled: Boolean, private val ctx: Context): CallAction.TogglableCallAction(R.id.bandyer_id_camera, mToggled, R.layout.bandyer_smartglass_call_action_item, ctx.getSmartGlassCallActionItemStyle(R.styleable.BandyerSDKDesign_SmartGlassDialogMenu_bandyer_smartGlassCameraStyle)) {

    override fun updateContentDescription(button: View?) {
        button?.contentDescription =
                if (toggled) ctx.resources.getString(R.string.bandyer_call_action_enable_camera_description)
                else ctx.resources.getString(R.string.bandyer_call_action_disable_camera_description)
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
 * Microphone smart glass call action item
 * @property toggled true or false to toggle
 * @constructor
 */
open class SMART_GLASS_MICROPHONE(mToggled: Boolean, private val ctx: Context): CallAction.TogglableCallAction(R.id.bandyer_id_microphone, mToggled, R.layout.bandyer_smartglass_call_action_item, ctx.getSmartGlassCallActionItemStyle(R.styleable.BandyerSDKDesign_SmartGlassDialogMenu_bandyer_smartGlassMicrophoneStyle)) {

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
 * Participants smart glass call action item
 * @constructor
 */
open class SMART_GLASS_PARTICIPANTS(ctx: Context): CallAction(R.id.bandyer_id_participants, R.layout.bandyer_smartglass_call_action_item, ctx.getSmartGlassCallActionItemStyle(R.styleable.BandyerSDKDesign_SmartGlassDialogMenu_bandyer_smartGlassParticipantsStyle))
