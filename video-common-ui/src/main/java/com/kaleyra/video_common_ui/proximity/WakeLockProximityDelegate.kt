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

package com.kaleyra.video_common_ui.proximity

import android.app.Application
import android.content.Context
import android.os.PowerManager
import com.kaleyra.video_common_ui.utils.CallExtensions.hasUsbInput
import com.kaleyra.video_common_ui.utils.CallExtensions.hasUsersWithCameraEnabled
import com.kaleyra.video_common_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.video_common_ui.utils.CallExtensions.isMyScreenShareEnabled
import com.kaleyra.video_common_ui.utils.CallExtensions.isNotConnected
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isOrientationLandscape
import com.kaleyra.video_common_ui.CallUI

interface WakeLockProximityDelegate {

    val application: Application

    val call: CallUI

    val isScreenTurnedOff: Boolean

    fun bind()

    fun destroy()

    fun tryTurnScreenOff()

    fun restoreScreenOn()
}

internal class WakeLockProximityDelegateImpl(
    override val application: Application,
    override val call: CallUI,
) : WakeLockProximityDelegate  {

    private var proximityWakeLock: PowerManager.WakeLock? = null

    override var isScreenTurnedOff: Boolean = false
        private set

    override fun bind() {
        val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, javaClass.simpleName)
        proximityWakeLock!!.setReferenceCounted(false)
    }

    override fun destroy() {
        proximityWakeLock?.release()
        proximityWakeLock = null
        isScreenTurnedOff = false
    }

    override fun tryTurnScreenOff() {
        val shouldAcquireProximityLock = shouldAcquireProximityLock()
        if (shouldAcquireProximityLock) {
            proximityWakeLock?.acquire(WakeLockTimeout)
        }
        isScreenTurnedOff = shouldAcquireProximityLock
    }

    override fun restoreScreenOn() {
        proximityWakeLock?.release()
        isScreenTurnedOff = false
    }

    private fun shouldAcquireProximityLock(): Boolean {
        val isDeviceInLandscape = application.isOrientationLandscape()
        return when {
            isDeviceInLandscape && call.isNotConnected() && call.isMyInternalCameraEnabled() -> false
            isDeviceInLandscape && call.hasUsersWithCameraEnabled() -> false
            call.isMyScreenShareEnabled() -> false
            call.hasUsbInput() -> false
            else -> true
        }
    }

    companion object {
        const val WakeLockTimeout = 60 * 60 * 1000L /*1 hour*/
    }
}