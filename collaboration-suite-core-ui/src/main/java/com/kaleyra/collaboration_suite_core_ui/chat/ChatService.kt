package com.kaleyra.collaboration_suite_core_ui.chat

import android.content.Intent
import com.bandyer.android_chat_sdk.ChatClientInstance
import com.bandyer.android_chat_sdk.api.ChatChannel
import com.kaleyra.collaboration_suite_core_ui.common.BoundService
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryObserver
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiObserver
import kotlinx.coroutines.flow.SharedFlow

class ChatService : BoundService(), ChatUIDelegate, DeviceStatusDelegate {

    private var activityClazz: Class<*>? = null

    private var chatClient: ChatClientInstance? = null

    private var batteryObserver: BatteryObserver? = null
    private var wifiObserver: WiFiObserver? = null

    override val channel: ChatChannel
        get() = TODO("Not yet implemented")

    override var usersDescription: UsersDescription = UsersDescription()

    override val battery: SharedFlow<BatteryInfo>
        get() = batteryObserver!!.observe()

    override val wifi: SharedFlow<WiFiInfo>
        get() = wifiObserver!!.observe()

    override fun onCreate() {
        super.onCreate()

        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryObserver?.stop()
        wifiObserver?.stop()

        activityClazz = null
        chatClient = null
        batteryObserver = null
        wifiObserver = null
    }

    fun bind(
        chatClient: ChatClientInstance,
        usersDescription: UsersDescription? = null,
        activityClazz: Class<*>
    ) {
        this.chatClient = chatClient
        this.usersDescription = usersDescription ?: UsersDescription()
        this.activityClazz = activityClazz
    }
}