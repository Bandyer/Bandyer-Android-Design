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

package com.kaleyra.collaboration_suite_glass_ui.termsandconditions

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditions
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate.Companion.EXTRA_CONFIGURATION
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.extensions.TermsAndConditionsExt.decline
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_glass_ui.GlassBaseActivity
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraActivityTermsAndConditionsGlassBinding
import com.kaleyra.collaboration_suite_glass_ui.status_bar_views.StatusBarView
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ActivityExtensions.enableImmersiveMode
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class GlassTermsAndConditionsActivity : GlassBaseActivity() {

    private lateinit var binding: KaleyraActivityTermsAndConditionsGlassBinding

    private val viewModel: TermsAndConditionsViewModel by viewModels()

    private var termsAndConditions: TermsAndConditions? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KaleyraActivityTermsAndConditionsGlassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val termsAndConditions = intent.extras?.getParcelable<TermsAndConditions>(EXTRA_CONFIGURATION)?.apply {
            termsAndConditions = this
        }
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        termsAndConditions?.decline()
    }

    private fun onConfig(configuration: TermsAndConditions) {
        val enableTilt = intent.getBooleanExtra("enableTilt", false)
        val termsAndConditionFragmentNavArgs = TermsAndConditionsFragmentArgs(enableTilt, configuration)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.kaleyra_nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(R.navigation.kaleyra_glass_terms_and_conditions_nav_graph, termsAndConditionFragmentNavArgs.toBundle())
    }

    override fun onDestinationChanged(destinationId: Int) = Unit

}
