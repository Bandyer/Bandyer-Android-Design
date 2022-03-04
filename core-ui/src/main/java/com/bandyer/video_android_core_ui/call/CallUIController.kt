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

package com.bandyer.video_android_core_ui.call

import androidx.fragment.app.FragmentActivity
import com.bandyer.video_android_core_ui.model.Permission
import com.bandyer.video_android_core_ui.model.Volume

interface CallUIController {
    suspend fun onRequestMicPermission(context: FragmentActivity): Permission

    suspend fun onRequestCameraPermission(context: FragmentActivity): Permission

    fun onAnswer()

    fun onHangup()

    fun onEnableCamera(enable: Boolean)

    fun onEnableMic(enable: Boolean)

    fun onSwitchCamera()

    fun onGetVolume(): Volume

    fun onSetVolume(value: Int)

    fun onSetZoom(value: Int)
}

//interface CallWaitingUIController {
//    fun onHangUpAndAnswer(newCall: Call)
//
//    fun onDecline(newCall: Call)
//
//    fun onHoldAndAnswer(newCall: Call)
//}
