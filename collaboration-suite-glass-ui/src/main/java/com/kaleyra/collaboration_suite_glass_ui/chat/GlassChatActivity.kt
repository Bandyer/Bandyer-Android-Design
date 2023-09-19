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

package com.kaleyra.collaboration_suite_glass_ui.chat

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOff
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_glass_ui.GlassTouchEventManager
import com.kaleyra.collaboration_suite_glass_ui.TouchEvent
import com.kaleyra.collaboration_suite_glass_ui.TouchEventListener
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatActivityGlassBinding
import com.kaleyra.collaboration_suite_glass_ui.status_bar_views.StatusBarView
import com.kaleyra.collaboration_suite_glass_ui.utils.currentNavigationFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ActivityExtensions.enableImmersiveMode
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.kaleyra.video_networking.connector.Connector
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class GlassChatActivity : ChatActivity(), GlassTouchEventManager.Listener {

    private lateinit var binding: KaleyraChatActivityGlassBinding

    override val viewModel: GlassChatViewModel by viewModels {
        GlassChatViewModel.provideFactory(::requestConfiguration)
    }
    private var glassTouchEventManager: GlassTouchEventManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KaleyraChatActivityGlassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        glassTouchEventManager = GlassTouchEventManager(this, this)
        if (DeviceUtils.isSmartGlass) enableImmersiveMode()
        turnScreenOn()

        viewModel.conversationState
            .onEach {
                if (it !is Connector.State.Disconnecting) return@onEach
                finishAndRemoveTask()
            }
            .launchIn(lifecycleScope)

        repeatOnStarted {
            viewModel
                .battery
                .onEach {
                    with(binding.kaleyraStatusBar) {
                        setBatteryChargingState(it.state == BatteryInfo.State.CHARGING)
                        setBatteryCharge(it.percentage)
                    }
                }
                .launchIn(this)

            viewModel
                .wifi
                .onEach {
                    binding.kaleyraStatusBar.setWiFiSignalState(
                        when {
                            it.state == WiFiInfo.State.DISABLED -> StatusBarView.WiFiSignalState.DISABLED
                            it.level == WiFiInfo.Level.NO_SIGNAL || it.level == WiFiInfo.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                            it.level == WiFiInfo.Level.FAIR || it.level == WiFiInfo.Level.GOOD -> StatusBarView.WiFiSignalState.MODERATE
                            else -> StatusBarView.WiFiSignalState.FULL
                        }
                    )
                }
                .launchIn(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOff()
        glassTouchEventManager = null
    }

    /**
     * @suppress
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean =
        if (glassTouchEventManager!!.toGlassTouchEvent(ev)) true
        else super.dispatchTouchEvent(ev)

    /**
     * @suppress
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        if (glassTouchEventManager!!.toGlassTouchEvent(event)) true
        else super.dispatchKeyEvent(event)

    override fun onGlassTouchEvent(glassEvent: TouchEvent): Boolean {
        val currentDest = supportFragmentManager.currentNavigationFragment as? TouchEventListener ?: return false
        return currentDest.onTouch(glassEvent)
    }
}