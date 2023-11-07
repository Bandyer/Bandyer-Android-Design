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

package com.kaleyra.video_glasses_sdk.termsandconditions

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.video_common_ui.termsandconditions.constants.Constants.EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION
import com.kaleyra.video_common_ui.termsandconditions.extensions.TermsAndConditionsExt.decline
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.video_glasses_sdk.GlassTouchEventManager
import com.kaleyra.video_glasses_sdk.TouchEvent
import com.kaleyra.video_glasses_sdk.TouchEventListener
import com.kaleyra.video_glasses_sdk.common.OnDestinationChangedListener
import com.kaleyra.video_glasses_sdk.status_bar_views.StatusBarView
import com.kaleyra.video_glasses_sdk.utils.currentNavigationFragment
import com.kaleyra.video_glasses_sdk.utils.extensions.ActivityExtensions.enableImmersiveMode
import com.kaleyra.video_glasses_sdk.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.video_glasses_sdk.R
import com.kaleyra.video_glasses_sdk.databinding.KaleyraActivityTermsAndConditionsGlassBinding
import com.kaleyra.video_utils.battery_observer.BatteryInfo
import com.kaleyra.video_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class GlassTermsAndConditionsActivity : AppCompatActivity(), OnDestinationChangedListener, com.kaleyra.video_glasses_sdk.GlassTouchEventManager.Listener {

    private lateinit var binding: KaleyraActivityTermsAndConditionsGlassBinding

    private val viewModel: TermsAndConditionsViewModel by viewModels {
        TermsAndConditionsViewModel.provideFactory(::requestConfiguration)
    }

    private var glassTouchEventManager: com.kaleyra.video_glasses_sdk.GlassTouchEventManager? = null

    private var termsAndConditions: TermsAndConditions? = null

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() { termsAndConditions?.decline() }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KaleyraActivityTermsAndConditionsGlassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val termsAndConditions: TermsAndConditions? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable(EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION, TermsAndConditions::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.extras?.getParcelable(EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION)
        }.apply { termsAndConditions = this }

        if (termsAndConditions != null) {
            onConfig(termsAndConditions)
        } else {
            finishAndRemoveTask()
        }

        if (DeviceUtils.isSmartGlass) enableImmersiveMode()
        turnScreenOn()

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
                            it.state == WiFiInfo.State.DISABLED                                     -> StatusBarView.WiFiSignalState.DISABLED
                            it.level == WiFiInfo.Level.NO_SIGNAL || it.level == WiFiInfo.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                            it.level == WiFiInfo.Level.FAIR || it.level == WiFiInfo.Level.GOOD      -> StatusBarView.WiFiSignalState.MODERATE
                            else                                                                    -> StatusBarView.WiFiSignalState.FULL
                        }
                    )
                }
                .launchIn(this)
        }

        glassTouchEventManager = com.kaleyra.video_glasses_sdk.GlassTouchEventManager(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        glassTouchEventManager = null
    }

    private fun onConfig(configuration: TermsAndConditions) {
        val enableTilt = intent.getBooleanExtra("enableTilt", false)
        val termsAndConditionFragmentNavArgs = TermsAndConditionsFragmentArgs(enableTilt, configuration)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.kaleyra_nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(R.navigation.kaleyra_glass_terms_and_conditions_nav_graph, termsAndConditionFragmentNavArgs.toBundle())
    }

    override fun onDestinationChanged(destinationId: Int) = Unit

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

    override fun onGlassTouchEvent(glassEvent: com.kaleyra.video_glasses_sdk.TouchEvent): Boolean {
        val currentDest = supportFragmentManager.currentNavigationFragment as? com.kaleyra.video_glasses_sdk.TouchEventListener ?: return false
        return currentDest.onTouch(glassEvent)
    }

}
