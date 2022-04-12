package com.kaleyra.collaboration_suite_glass_ui.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.kaleyra.collaboration_suite_core_ui.chat.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.chat.ChatService
import com.kaleyra.collaboration_suite_core_ui.chat.ChatUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.common.OnDestinationChangedListener
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatActivityGlassBinding

class GlassChatActivity : ChatActivity(), OnDestinationChangedListener {

    private lateinit var binding: KaleyraChatActivityGlassBinding

    private var service: ChatService? = null

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            service as ChatUIDelegate,
            service as DeviceStatusDelegate
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.kaleyra_chat_activity_glass)

        // TODO move in onServiceBound
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
//        enableImmersiveMode()
    }

    override fun onServiceBound(service: ChatService) {
        this.service = service
    }

    override fun onDestinationChanged(destinationId: Int) = Unit
}