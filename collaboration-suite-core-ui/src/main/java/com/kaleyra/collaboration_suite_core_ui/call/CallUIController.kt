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

package com.kaleyra.collaboration_suite_core_ui.call

import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite_core_ui.model.Permission
import com.kaleyra.collaboration_suite_core_ui.model.Volume
import kotlinx.coroutines.flow.StateFlow

/**
 * Call UI controller. It contains methods called when the user interacts with the ui.
 */
interface CallUIController {

    /**
     * The mic permission flow
     */
    val micPermission: StateFlow<Permission>

    /**
     * The cam permission flow
     */
    val camPermission: StateFlow<Permission>

    /**
     * The volume
     */
    val volume: Volume

    /**
     * On request mic permission
     *
     * @param context activity
     * @return [Permission]
     */
    fun onRequestMicPermission(context: FragmentActivity)

    /**
     * On request camera permission
     *
     * @param context activity
     * @return [Permission]
     */
    fun onRequestCameraPermission(context: FragmentActivity)


    /**
     * On answer
     *
     */
    fun onAnswer()

    /**
     * On hangup
     *
     */
    fun onHangup()

    /**
     * On enable camera
     *
     * @param context Context
     * @param enable true to enable, false otherwise
     */
    suspend fun onEnableCamera(context: FragmentActivity, enable: Boolean)

    /**
     * On enable mic
     *
     * @param context Context
     * @param enable true to enable, false otherwise
     */
    suspend fun onEnableMic(context: FragmentActivity,enable: Boolean)

    /**
     * On switch camera
     *
     */
    fun onSwitchCamera()

    /**
     * On set volume
     *
     * @param value level of volume to set
     */
    fun onSetVolume(value: Int)

    /**
     * On set zoom
     *
     * @param value zoom level
     */
    fun onSetZoom(value: Float)
}

//interface CallWaitingUIController {
//    fun onHangUpAndAnswer(newCall: Call)
//
//    fun onDecline(newCall: Call)
//
//    fun onHoldAndAnswer(newCall: Call)
//}
