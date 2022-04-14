package com.kaleyra.collaboration_suite_glass_ui.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.bandyer.android_chat_sdk.api.ChatChannel
import com.bandyer.android_chat_sdk.api.ChatMessages
import com.bandyer.android_chat_sdk.api.ChatParticipant
import com.bandyer.android_chat_sdk.api.ChatParticipants
import com.bandyer.android_chat_sdk.chat_service.model.Message
import com.bandyer.android_chat_sdk.persistence.entities.ChatMessage
import com.kaleyra.collaboration_suite_core_ui.chat.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.chat.ChatService
import com.kaleyra.collaboration_suite_core_ui.chat.ChatUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
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
import java.time.Instant

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

var mockMessagesList = listOf(
    ChatMessage(Message(
        "mId1",
        "chatChannelId",
        "user1",
        "Come se fosse antani con lo scappellamento a sinistra",
        Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 3600 * 1000),
        attributes = "{}"
    )),
    ChatMessage(Message(
        "mId1",
        "chatChannelId",
        "user2",
        "Tuttavia, perché voi intendiate da dove sia nato tutto questo errore, di quelli che incolpano il piacere ed esaltano il dolore, io spiegherò tutta la questione, e presenterò le idee espresse dal famoso esploratore della verità, vorrei quasi dire dal costruttore della felicità umana. Nessuno, infatti, detesta, odia, o rifugge il piacere in quanto tale, solo perché è piacere, ma perché grandi sofferenze colpiscono quelli che non sono capaci di raggiungere il piacere attraverso la ragione; e al contrario, non c'è nessuno che ami, insegua, voglia raggiungere il dolore in se stesso, soltanto perché è dolore, ma perché qualche volta accadono situazioni tali per cui attraverso la sofferenza o il dolore si cerca di raggiungere un qualche grande piacere. Concentrandoci su casi di piccola importanza: chi di noi intraprende un esercizio ginnico, se non per ottenerne un qualche vantaggio? E d'altra parte, chi avrebbe motivo di criticare colui che desidera provare un piacere cui non segua nessun fastidio, o colui che fugge un dolore che non produce nessun piacere?",
        Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 24 * 3600 * 1000),
        attributes = "{}"
    )),
    ChatMessage(Message(
        "mId1",
        "chatChannelId",
        "user3",
        "Mi piacciono i treni",
        Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 90 * 3600 * 1000),
        attributes = "{}"
    ))
)

val mockFlowMessageList = MutableStateFlow(mockMessagesList)
var newMessagesLoaded = false

var mockMessages = object : ChatMessages {
    override val channelId: Any = "chatChannelId"
    override val messageList: StateFlow<List<ChatMessage>> = mockFlowMessageList
    override fun sendTextMessage(message: String) = Unit
    override fun loadPrevious(success: () -> Unit, error: (Exception) -> Unit) {
        if (newMessagesLoaded) return
        newMessagesLoaded = true
        val list = messageList.value.toMutableList()
        list.add(
            ChatMessage(Message(
                "mId4",
                "chatChannelId",
                "user1",
                "Franchino fammi volare, sopra le nuvole, magiaaaaaa",
                Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 180 * 3600 * 1000),
                attributes = "{}"
            ))
        )
        list.add(
            ChatMessage(Message(
                "mId5",
                "chatChannelId",
                "user2",
                "Anatra animale definitivo",
                Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 270 * 3600 * 1000),
                attributes = "{}"
            ))
        )
        mockFlowMessageList.value = list
        success.invoke()
    }
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