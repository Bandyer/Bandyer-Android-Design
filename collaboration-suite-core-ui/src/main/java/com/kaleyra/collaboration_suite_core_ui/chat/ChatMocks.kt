package com.kaleyra.collaboration_suite_core_ui.chat

import com.bandyer.android_chat_sdk.ChatClientInstance
import com.bandyer.android_chat_sdk.ChatClientObserver
import com.bandyer.android_chat_sdk.ChatClientState
import com.bandyer.android_chat_sdk.OnIncomingChatMessageObserver
import com.bandyer.android_chat_sdk.OnNotificationObserver
import com.bandyer.android_chat_sdk.api.ChatChannel
import com.bandyer.android_chat_sdk.api.ChatParticipant
import com.bandyer.android_chat_sdk.chat_service.model.Message
import com.bandyer.android_chat_sdk.model.ChatUser
import com.bandyer.android_chat_sdk.persistence.ChannelRepositoryInstance
import com.bandyer.android_chat_sdk.persistence.ChatMessageRepository
import com.bandyer.android_chat_sdk.persistence.entities.ChatMessage
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_networking.Configuration
import com.kaleyra.collaboration_suite_networking.Environment
import com.kaleyra.collaboration_suite_networking.Region
import com.kaleyra.collaboration_suite_utils.logging.PriorityLogger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.time.Instant

var mockMessagesList = listOf(
    ChatMessage(
        Message(
        "mId1",
        "chatChannelId",
        "user1",
        "Come se fosse antani con lo scappellamento a sinistra",
        Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 3600 * 1000),
        attributes = "{}"
    )
    ),
    ChatMessage(
        Message(
        "mId2",
        "chatChannelId",
        "user2",
        "Tuttavia, perché voi intendiate da dove sia nato tutto questo errore, di quelli che incolpano il piacere ed esaltano il dolore, io spiegherò tutta la questione, e presenterò le idee espresse dal famoso esploratore della verità, vorrei quasi dire dal costruttore della felicità umana. Nessuno, infatti, detesta, odia, o rifugge il piacere in quanto tale, solo perché è piacere, ma perché grandi sofferenze colpiscono quelli che non sono capaci di raggiungere il piacere attraverso la ragione; e al contrario, non c'è nessuno che ami, insegua, voglia raggiungere il dolore in se stesso, soltanto perché è dolore, ma perché qualche volta accadono situazioni tali per cui attraverso la sofferenza o il dolore si cerca di raggiungere un qualche grande piacere. Concentrandoci su casi di piccola importanza: chi di noi intraprende un esercizio ginnico, se non per ottenerne un qualche vantaggio? E d'altra parte, chi avrebbe motivo di criticare colui che desidera provare un piacere cui non segua nessun fastidio, o colui che fugge un dolore che non produce nessun piacere?",
        Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 24 * 3600 * 1000),
        attributes = "{}"
    )
    ),
    ChatMessage(
        Message(
        "mId3",
        "chatChannelId",
        "user3",
        "Mi piacciono i treni",
        Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 90 * 3600 * 1000),
        attributes = "{}"
    )
    )
)

val mockFlowMessageList = MutableStateFlow(mockMessagesList).apply {
    MainScope().launch {
        delay(3000L)
        value = value.toMutableList().apply {
            add(0,
                ChatMessage(
                    Message(
                    "mId0",
                    "chatChannelId",
                    "user0",
                    "Dummy text.",
                    Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli()),
                    attributes = "{}"
                )
                )
            )
        }
    }
}
var newMessagesLoaded = false

var mockChannel = object : ChatChannel {
    override val id: Any = "chatChannelId"
    override val participants: StateFlow<List<ChatParticipant>> = MutableStateFlow(listOf())
    override val messages: StateFlow<List<ChatMessage>> = mockFlowMessageList
    override fun subscribe() = Unit
    override fun unsubscribe() = Unit
    override fun sendTextMessage(message: String) = Unit
    override fun fetch(success: () -> Unit, error: (String) -> Unit) {
        if (newMessagesLoaded) return
        newMessagesLoaded = true
        val list = messages.value.toMutableList()
        list.add(
            ChatMessage(
                Message(
                    "mId4",
                    "chatChannelId",
                    "user1",
                    "Franchino fammi volare, sopra le nuvole, magiaaaaaa",
                    Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 180 * 3600 * 1000),
                    attributes = "{}"
                )
            )
        )
        list.add(
            ChatMessage(
                Message(
                    "mId5",
                    "chatChannelId",
                    "user2",
                    "Anatra animale definitivo",
                    Iso8601.parseMillisToIso8601(Instant.now().toEpochMilli() - 270 * 3600 * 1000),
                    attributes = "{}"
                )
            )
        )
        mockFlowMessageList.value = list
        success.invoke()
    }
}

val mockChatClient = object : ChatClientInstance {
    override val state: StateFlow<ChatClientState> = MutableStateFlow(ChatClientState.Disconnected)
    override suspend fun createChannel(users: List<ChatUser>): ChatChannel = mockChannel
    override suspend fun deleteChannel(users: List<ChatUser>) = Unit
    override fun fetchChannels(pageSize: Int, success: () -> Unit, error: (String) -> Unit) = Unit
    override val configuration: Configuration = object : Configuration {
        override val appId: String = ""
        override val environment: Environment = object : Environment {
            override val name: String = ""
        }
        override val httpStack: OkHttpClient = OkHttpClient()
        override val logger: PriorityLogger? = null
        override val region: Region = Region.Eu
    }
    override var userId: String? = null
    override var channelRepository: ChannelRepositoryInstance? = null
    override var chatMessageRepository: ChatMessageRepository? = null
    override fun addChatClientStateObserver(chatClientObserver: ChatClientObserver) = Unit
    override fun removeChatClientStateObserver(chatClientObserver: ChatClientObserver) = Unit
    override fun addIncomingChatMessageObserver(observer: OnIncomingChatMessageObserver) = Unit
    override fun removeIncomingChatMessageObserver(observer: OnIncomingChatMessageObserver) = Unit
    override fun removeObservers() = Unit
    override fun connect(userId: String, accessToken: String) = Unit
    override fun disconnect() = Unit
    override fun updateToken(
        accessToken: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) = Unit
    override fun handleNotification(data: String, onNotificationObserver: OnNotificationObserver) = Unit
    override fun clearUserCache() = Unit
    override val channels: StateFlow<List<ChatChannel>>
        get() = TODO("Not yet implemented")
}