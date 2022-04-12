package com.kaleyra.collaboration_suite_glass_ui.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.bandyer.android_chat_sdk.api.ChatChannel
import com.bandyer.android_chat_sdk.api.ChatMessages
import com.bandyer.android_chat_sdk.api.ChatParticipant
import com.bandyer.android_chat_sdk.api.ChatParticipants
import com.bandyer.android_chat_sdk.persistence.entities.ChatMessage
import com.kaleyra.collaboration_suite_core_ui.chat.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.chat.ChatService
import com.kaleyra.collaboration_suite_core_ui.chat.ChatUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.common.OnDestinationChangedListener
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatActivityGlassBinding
import com.kaleyra.collaboration_suite_glass_ui.status_bar_views.StatusBarView
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GlassChatActivity : ChatActivity(), OnDestinationChangedListener {

    private lateinit var binding: KaleyraChatActivityGlassBinding

    private var service: ChatService? = null

    private val viewModel: ChatViewModel by viewModels {
//        ChatViewModelFactory(
//            service as ChatUIDelegate,
//            service as DeviceStatusDelegate
//        )
        ChatViewModelFactory(
            mockChatUIDelegate,
            mockDeviceStatusDelegate
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

var mockParticipants = object : ChatParticipants {
    override val participants: StateFlow<List<ChatParticipant>> = MutableStateFlow(listOf())
}

var mockMessages = object : ChatMessages {
    override val channelId: Any = "chatChannelId"
    override val messageList: StateFlow<List<ChatMessage>> = MutableStateFlow(listOf())
    override fun sendTextMessage(message: String) = Unit
    override fun loadPrevious(success: () -> Unit, error: (Exception) -> Unit) = Unit
}

var mockChannel = object : ChatChannel {
    override val id: Any = "chatChannelId"
    override val chatMessages: ChatMessages = mockMessages
    override val chatParticipants: ChatParticipants = mockParticipants
    override fun subscribe() = Unit
    override fun unsubscribe() = Unit
}

var mockChatUIDelegate = object : ChatUIDelegate {
    override val channel: ChatChannel = mockChannel
    override val usersDescription: UsersDescription = UsersDescription()
}

var mockDeviceStatusDelegate = object : DeviceStatusDelegate {
    override val battery: SharedFlow<BatteryInfo> = MutableStateFlow(BatteryInfo(BatteryInfo.State.CHARGING, BatteryInfo.Plugged.AC))
    override val wifi: SharedFlow<WiFiInfo> = MutableStateFlow(WiFiInfo(WiFiInfo.State.ENABLED, WiFiInfo.Level.EXCELLENT))
}