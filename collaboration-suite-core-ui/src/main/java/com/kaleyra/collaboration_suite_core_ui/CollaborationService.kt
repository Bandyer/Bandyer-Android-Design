package com.kaleyra.collaboration_suite_core_ui

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.call.CallController
import com.kaleyra.collaboration_suite_core_ui.call.CallNotificationDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallStreamDelegate
import com.kaleyra.collaboration_suite_core_ui.call.CallUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.BoundService
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
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
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

class CollaborationService: BoundService(),
    CallUIDelegate,
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

    private var phoneBoxJob: Job? = null

    private var batteryObserver: BatteryObserver? = null

    private var wifiObserver: WiFiObserver? = null

    private var callActivityClazz: Class<*>? = null

    private var isServiceInForeground: Boolean = false

    private val _call: MutableSharedFlow<Call> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    override val call: SharedFlow<Call> get() = _call

    override var currentCall: Call? = null

    private var _callAudioManager: CallAudioManager? = null
    override val callAudioManager: CallAudioManager get() = _callAudioManager!!

    override var usersDescription: UsersDescription = UsersDescription()

    override var isAppInForeground: Boolean = false

    override val battery: SharedFlow<BatteryInfo> get() = batteryObserver!!.observe()

    override val wifi: SharedFlow<WiFiInfo> get() = wifiObserver!!.observe()

    override fun onCreate() {
        super<BoundService>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)
        CallNotificationActionReceiver.actionDelegate = this
        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)
        _callAudioManager = CallAudioManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super<BoundService>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(this)
        clearNotification()
        phoneBoxJob?.cancel()
        currentCall?.end()
        phoneBox?.disconnect()
        batteryObserver?.stop()
        wifiObserver?.stop()
        CallNotificationActionReceiver.actionDelegate = null
        currentCall = null
        _callAudioManager = null
        phoneBox = null
        phoneBoxJob = null
        callActivityClazz = null
        batteryObserver = null
        wifiObserver = null
    }

    fun bind(
        phoneBox: PhoneBox,
        usersDescription: UsersDescription? = null,
        activityClazz: Class<*>
    ) {
        this.phoneBox = phoneBox
        this.usersDescription = usersDescription ?: UsersDescription()
        this.callActivityClazz = activityClazz
        phoneBoxJob?.cancel()
        phoneBoxJob = listenToCalls(phoneBox, this.usersDescription, this.callActivityClazz!!)
    }

    private fun listenToCalls(
        phoneBox: PhoneBox,
        usersDescription: UsersDescription,
        callActivityClazz: Class<*>
    ) =
        phoneBox.call.onEach { call ->
            if (currentCall != null || call.state.value is Call.State.Disconnected.Ended) return@onEach
            currentCall = call
            _call.emit(call)

            call.state
                .takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion {
                    currentCall = null
                    if (isAppInForeground) return@onCompletion
                    stopSelf()
//                    Log.e("CollaborationService", "stopping service onCompletion")
                }
                .launchIn(lifecycleScope)

            setUpCallStreams(this@CollaborationService, call)
            syncNotificationWithCallState(
                this@CollaborationService,
                call,
                usersDescription,
                callActivityClazz
            )

            if (!shouldShowCallUI(call)) return@onEach
            UIProvider.showCall(callActivityClazz)
        }.launchIn(lifecycleScope)

    private fun shouldShowCallUI(call: Call): Boolean =
        isAppInForeground && (!this@CollaborationService.isSilent() || call.participants.value.let { it.me == it.creator() })

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        if (currentCall != null && currentCall!!.state.value !is Call.State.Disconnected.Ended) return
        stopSelf()
//        Log.e("CollaborationService", "stopping service onStop")
    }

    ////////////////////////////////////////////
    // Application.ActivityLifecycleCallbacks //
    ////////////////////////////////////////////
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.javaClass != callActivityClazz) return
        currentCall?.also { publishMyStream(activity as FragmentActivity, it) }
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity.javaClass != callActivityClazz || isServiceInForeground) return
        lifecycleScope.launch {
            currentCall ?: return@launch
            moveNotificationToForeground(
                currentCall!!,
                usersDescription,
                callActivityClazz!!
            )
        }
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    ///////////////////////////////////////////////////
    // CallNotificationActionReceiver.ActionDelegate //
    ///////////////////////////////////////////////////
    override fun onAnswerAction() {
        currentCall?.connect()
    }

    override fun onHangUpAction() {
        currentCall?.end()
    }

    override fun onScreenShareAction() = Unit

    /////////////////////
    // CallController //
    ////////////////////
    override fun onHangup() {
        super.onHangup()
        clearNotification()
    }

    //////////////////////////////
    // CallNotificationDelegate //
    //////////////////////////////
    override fun showNotification(notification: Notification, showInForeground: Boolean) {
        if (showInForeground) {
            startForeground(CALL_NOTIFICATION_ID, notification).also {
                isServiceInForeground = true
            }
        } else NotificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    override fun clearNotification() {
        stopForeground(true).also { isServiceInForeground = false }
        NotificationManager.cancelNotification(CALL_NOTIFICATION_ID)
    }
}