package com.kaleyra.collaboration_suite_glass_ui.userdataconsentagreement

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_glass_ui.GlassBaseActivity
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraActivityUserDataConsentAgreementGlassBinding
import com.kaleyra.collaboration_suite_glass_ui.status_bar_views.StatusBarView
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ActivityExtensions.enableImmersiveMode
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class GlassUserDataConsentAgreementActivity : GlassBaseActivity() {

    private lateinit var binding: KaleyraActivityUserDataConsentAgreementGlassBinding

    private val viewModel: UserDataConsentAgreementViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KaleyraActivityUserDataConsentAgreementGlassBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    override fun onDestinationChanged(destinationId: Int) = Unit

}
