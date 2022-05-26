package com.kaleyra.collaboration_suite_core_ui

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.chatbox.Chat
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.call.CallController
import com.kaleyra.collaboration_suite_core_ui.call.CallNotificationDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallStreamDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallUIDelegate
import com.kaleyra.collaboration_suite_core_ui.chat.ChatUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.BoundService
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.ChatNotification
import com.kaleyra.collaboration_suite_core_ui.notification.ChatNotificationManager
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.collaboration_suite_utils.audio.CallAudioManager
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryObserver
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

/**
 * The CollaborationService
 */
class CollaborationService : BoundService(),
    CallUIDelegate,
    ChatUIDelegate,
    CallStreamDelegate,
    CallNotificationDelegate,
    DeviceStatusDelegate,
    CallController,
    Application.ActivityLifecycleCallbacks,
    CallNotificationActionReceiver.ActionDelegate {

    private companion object {
        const val CALL_NOTIFICATION_ID = 22
    }

    private var phoneBox: PhoneBox? = null

    private var chatBox: ChatBox? = null

    private var phoneBoxJob: Job? = null

    private var chatBoxJob: Job? = null
    private var chatBoxMessagesJobs: MutableList<Job> = mutableListOf()

    private var batteryObserver: BatteryObserver? = null

    private var wifiObserver: WiFiObserver? = null

    private var callActivityClazz: Class<*>? = null

    private var chatActivityClazz: Class<*>? = null

    private var isServiceInForeground: Boolean = false

    private var isChatInForeground: Boolean = false

    private val _call: MutableSharedFlow<CallUI> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    override val call: SharedFlow<CallUI> get() = _call

    private var _chat: Chat? = null
    override val chat: Chat get() = _chat!!

    override var currentCall: CallUI? = null

    private var _callAudioManager: CallAudioManager? = null
    override val callAudioManager: CallAudioManager get() = _callAudioManager!!

    override var callUsersDescription: UsersDescription = UsersDescription()

    override var chatUsersDescription: UsersDescription = UsersDescription()

    override var isAppInForeground: Boolean = false

    override val battery: SharedFlow<BatteryInfo> get() = batteryObserver!!.observe()

    override val wifi: SharedFlow<WiFiInfo> get() = wifiObserver!!.observe()

    /**
     * @suppress
     */
    override fun onCreate() {
        super<BoundService>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)
        CallNotificationActionReceiver.actionDelegate = this
        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)
        _callAudioManager = CallAudioManager(this)
    }

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super<BoundService>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(this)
        clearNotification()
        phoneBoxJob?.cancel()
        chatBoxJob?.cancel()
        chatBoxMessagesJobs.forEach { it.cancel() }
        chatBoxMessagesJobs.clear()
        currentCall?.end()
        batteryObserver?.stop()
        wifiObserver?.stop()
        CollaborationUI.disconnect()
        CallNotificationActionReceiver.actionDelegate = null
        currentCall = null
        _callAudioManager = null
        _chat = null
        phoneBox = null
        chatBox = null
        phoneBoxJob = null
        chatBoxJob = null
        callActivityClazz = null
        batteryObserver = null
        wifiObserver = null
    }

    /**
     * Bind the service to a phone box
     *
     * @param phoneBox The phonebox
     * @param callUsersDescription The user description. Optional.
     * @param callActivityClazz The call activity class
     */
    fun bindCall(
        phoneBox: PhoneBoxUI,
        call: CallUI,
        callUsersDescription: UsersDescription? = null,
        callActivityClazz: Class<*>
    ) {

        if (currentCall != null || call.state.value is Call.State.Disconnected.Ended) return
        this.phoneBox = phoneBox
        this.callUsersDescription = callUsersDescription ?: UsersDescription()
        this.callActivityClazz = callActivityClazz
        phoneBoxJob?.cancel()
        phoneBoxJob = lifecycleScope.launch {
            currentCall = call
            _call.emit(call)
            call.state.takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion {
                    currentCall = null
                    if (isAppInForeground) return@onCompletion
                    stopSelf()
                }.launchIn(lifecycleScope)

            setUpCallStreams(this@CollaborationService, call)

            syncNotificationWithCallState(
                this@CollaborationService,
                call,
                this@CollaborationService.callUsersDescription,
                callActivityClazz
            )
        }
    }


    fun bindChatChannel(
        chat: Chat,
        chatUsersDescription: UsersDescription? = null,
        chatActivityClazz: Class<*>
    ) {
        this._chat = chat
        this.chatUsersDescription = chatUsersDescription ?: UsersDescription()
        this.chatActivityClazz = chatActivityClazz
    }

    fun bindCustomChatNotification(
        chatBox: ChatBox,
        chatNotificationActivityClazz: Class<*>
    ) {
        this.chatBox = chatBox
        chatBoxJob?.cancel()
        chatBoxMessagesJobs.forEach { it.cancel() }
        chatBoxMessagesJobs.clear()
        chatBoxJob = listenToChats(chatBox, ChatNotificationManager(chatNotificationActivityClazz))
    }

    private fun listenToChats(
        chatBox: ChatBox,
        chatNotificationManager: ChatNotificationManager
    ): Job {
        val hashMap = hashMapOf<String, String>()
        return chatBox.chats
            .onEach { chats ->
                chatBoxMessagesJobs.forEach {
                    it.cancel()
                    it.join()
                }
                chatBoxMessagesJobs.clear()
                chats.forEach { chat ->
                    chatBoxMessagesJobs += chat.messages
                        .onSubscription { Log.e("CollaborationService", "Subscribe job chat: ${chat.id}") }
                        .onEach onEachMessages@{ msgs ->
                            val msgId = chat.messages.value.list.firstOrNull()?.id
                            val msgContent = (chat.messages.value.list.firstOrNull()?.content as? Message.Content.Text)?.message
                            Log.e("CollaborationService", "last message: id: $msgId, content: $msgContent")

                            msgs.other.firstOrNull { it.state.value is Message.State.Received }
                                ?.also {
                                    if (hashMap[chat.id] == it.id) return@onEachMessages
                                    hashMap[chat.id] = it.id
//                            _newMessages.emit(Pair(chat, it))

                                    Log.e("CollaborationService", "ChatId: ${chat.id}, MsgId: ${it.id}")

                                    if (isChatInForeground) return@onEachMessages

                                    val userId = it.creator.userId
                                    val username = callUsersDescription.name(listOf(userId))
                                    val message = (chat.messages.value.list.firstOrNull()?.content as? Message.Content.Text)?.message ?: ""
                                    val imageUri = callUsersDescription.image(listOf(userId))

                                    chatNotificationManager.notify(
                                        ChatNotification(
                                            username,
                                            userId,
                                            message,
                                            imageUri,
                                            chat.participants.value.others.map { part -> part.userId }
                                        )
                                    )
                                }
                        }.launchIn(lifecycleScope)
                }
            }.launchIn(lifecycleScope)
    }

    fun canShowCallActivity(call: Call): Boolean =
        isAppInForeground && (!this@CollaborationService.isSilent() || call.participants.value.let { it.me == it.creator() })

    /**
     * @suppress
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        if (currentCall != null && currentCall!!.state.value !is Call.State.Disconnected.Ended) return
        stopSelf()
//        Log.e("CollaborationService", "stopping service onStop")
    }

    ////////////////////////////////////////////
    // Application.ActivityLifecycleCallbacks //
    ////////////////////////////////////////////
    /**
     * @suppress
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.javaClass != callActivityClazz) return
        currentCall?.also { publishMyStream(activity as FragmentActivity, it) }
    }

    /**
     * @suppress
     */
    override fun onActivityStarted(activity: Activity) {
        when {
            activity.javaClass == callActivityClazz && !isServiceInForeground -> lifecycleScope.launch {
                currentCall ?: return@launch
                moveNotificationToForeground(
                    currentCall!!,
                    callUsersDescription,
                    callActivityClazz!!
                )
            }
            activity.javaClass == chatActivityClazz -> isChatInForeground = true
        }
    }

    /**
     * @suppress
     */
    override fun onActivityResumed(activity: Activity) = Unit

    /**
     * @suppress
     */
    override fun onActivityPaused(activity: Activity) = Unit

    /**
     * @suppress
     */
    override fun onActivityStopped(activity: Activity) {
        if (activity.javaClass != chatActivityClazz) return
        isChatInForeground = false
    }

    /**
     * @suppress
     */
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    /**
     * @suppress
     */
    override fun onActivityDestroyed(activity: Activity) = Unit

    ///////////////////////////////////////////////////
    // CallNotificationActionReceiver.ActionDelegate //
    ///////////////////////////////////////////////////
    /**
     * @suppress
     */
    override fun onAnswerAction() {
        currentCall?.connect()
    }

    /**
     * @suppress
     */
    override fun onHangUpAction() {
        currentCall?.end()
    }

    /**
     * @suppress
     */
    override fun onScreenShareAction() = Unit

    /////////////////////
    // CallController //
    ////////////////////
    /**
     * @suppress
     */
    override fun onHangup() {
        super.onHangup()
        clearNotification()
    }

    //////////////////////////////
    // CallNotificationDelegate //
    //////////////////////////////
    /**
     * @suppress
     */
    override fun showNotification(notification: Notification, showInForeground: Boolean) {
        if (showInForeground) {
            startForeground(CALL_NOTIFICATION_ID, notification).also {
                isServiceInForeground = true
            }
        } else NotificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    /**
     * @suppress
     */
    override fun clearNotification() {
        stopForeground(true).also { isServiceInForeground = false }
        NotificationManager.cancelNotification(CALL_NOTIFICATION_ID)
    }
}