package com.kaleyra.collaboration_suite_core_ui

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.call.CallController
import com.kaleyra.collaboration_suite_core_ui.call.CallStreamDelegate
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class CollaborationService : BoundService(), CallStreamDelegate,
    CallController,
    DefaultLifecycleObserver,
    Application.ActivityLifecycleCallbacks,
    CallNotificationActionReceiver.ActionDelegate, DeviceStatusDelegate {

    private companion object {
        const val CALL_NOTIFICATION_ID = 22
    }

    private var mCallAudioManager: CallAudioManager? = null
    private var mBatteryObserver: BatteryObserver? = null
    private var mWifiObserver: WiFiObserver? = null
    private var mIsServiceInForeground: Boolean = false
    private var mIsAppInForeground: Boolean = false
    private var mCallActivityClazz: Class<*>? = null

    override val callAudioManager: CallAudioManager get() = mCallAudioManager!!
    override var usersDescription: UsersDescription = UsersDescription()
    override val currentCall: Call
        get() = TODO("Not yet implemented")
    override val battery: SharedFlow<BatteryInfo> get() = mBatteryObserver!!.observe()
    override val wifi: SharedFlow<WiFiInfo> get() = mWifiObserver!!.observe()

//    val ongoingCall: MutableSharedFlow<Call>
//        get() = TODO("Not yet implemented")

    var phoneBoxJob: Job? = null

    override fun onCreate() {
        super<BoundService>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)
        CallNotificationActionReceiver.actionDelegate = this
        mBatteryObserver = BatteryObserver(this)
        mWifiObserver = WiFiObserver(this)
        mCallAudioManager = CallAudioManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super<BoundService>.onDestroy()
        clearNotification()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(this)
        mBatteryObserver?.stop()
        mWifiObserver?.stop()
        CallNotificationActionReceiver.actionDelegate = null
        mBatteryObserver = null
        mWifiObserver = null
        mCallAudioManager = null
    }

    // CallService
    fun bind(
        phoneBox: PhoneBox,
        usersDescription: UsersDescription? = null,
        activityClazz: Class<*>
    ) {
        this.phoneBox = phoneBox
        this.usersDescription = usersDescription ?: UsersDescription()
        this.mCallActivityClazz = activityClazz
        phoneBoxJob?.cancel()
        phoneBoxJob = setUpCall(phoneBox)
    }

    //        ongoingCall.onEach onEachCall@{ call ->
//            if (
//                currentCall != null ||
//                call.state.value is Call.State.Disconnected.Ended) return@onEachCall

//            currentCall = call
//            call.state
//                .takeWhile { it !is Call.State.Disconnected.Ended }
//                .onCompletion { currentCall = null }
//                .launchIn(lifecycleScope)


//            ongoingCall.emit(call)
//            val participants = call.participants.value
//            if (isAppInForeground && (!context.isSilent() || participants.me == participants.creator()))
//                UIProvider.showCall(activityClazz!!)

//        }.launchIn(coroutineScope)

    fun dispose() {
        phoneBoxJob?.cancel()
        currentCall?.end()
        phoneBox?.disconnect()

        mCallActivityClazz = null
        phoneBoxJob = null
        currentCall = null
        phoneBox = null
    }

    //////////////////////////////
    // DefaultLifecycleObserver //
    //////////////////////////////
    override fun onStart(owner: LifecycleOwner) {
        mIsAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        mIsAppInForeground = false
    }

    ////////////////////////////////////////////
    // Application.ActivityLifecycleCallbacks //
    ////////////////////////////////////////////
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity.javaClass != mCallActivityClazz) return
        publishMySelf(currentCall, activity as FragmentActivity)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity.javaClass != mCallActivityClazz || mIsServiceInForeground) return
        startForegroundIfIncomingCall()
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    //////////////////
    // CallStreamDelegate //
    //////////////////
    override fun onIncomingCall() {
        lifecycleScope.launch {
            showIncomingCallNotification(
                usersDescription = callerDescription(),
                isGroupCall = isGroupCall(),
                isHighPriority = !mIsAppInForeground || isSilent(),
                moveToForeground = mIsAppInForeground
            )
        }
    }

    override fun onOutgoingCall() {
        lifecycleScope.launch {
            showOutgoingCallNotification(
                usersDescription = calleeDescription(),
                isGroupCall = isGroupCall()
            )
        }
    }

    override fun onOngoingCall(isConnecting: Boolean) {
        lifecycleScope.launch {
            showOnGoingCallNotification(
                usersDescription = calleeDescription(),
                isGroupCall = isGroupCall(),
                isCallRecorded = currentCall.extras.recording is Call.Recording.OnConnect,
                isConnecting = isConnecting
            )
        }
    }

    override fun onEndCall() {
        clearNotification()
    }

    ///////////////////////////////////////////////////
    // CallNotificationActionReceiver.ActionDelegate //
    ///////////////////////////////////////////////////
    override fun onAnswerAction() {
        currentCall.connect()
    }

    override fun onHangUpAction() {
        currentCall.end()
    }

    override fun onScreenShareAction() = Unit

    /////////////////////
    // CallController //
    ////////////////////
    override fun onHangup() {
        super.onHangup()
        clearNotification()
    }

    /////////////////////
    // Local functions //
    /////////////////////
    private fun startForegroundIfIncomingCall() =
        lifecycleScope.launch {
            val participants = currentCall.participants.value
            if (currentCall.state.value !is Call.State.Disconnected || participants.me == participants.creator()) return@launch
            showIncomingCallNotification(
                callerDescription(),
                isGroupCall(),
                isHighPriority = false,
                moveToForeground = true
            )
        }

    private fun showNotification(notification: Notification, moveToForeground: Boolean) {
        if (moveToForeground) startForegroundLocal(notification)
        else NotificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    private fun startForegroundLocal(notification: Notification) =
        startForeground(CALL_NOTIFICATION_ID, notification).also { mIsServiceInForeground = true }

    private fun stopForegroundLocal() =
        stopForeground(true).also { mIsServiceInForeground = false }

    private fun callParticipants() = currentCall.participants.value

    private fun isGroupCall() = callParticipants().others.count() > 1

    private suspend fun callerDescription() =
        usersDescription.name(listOf(callParticipants().creator()?.userId ?: ""))

    private suspend fun calleeDescription() =
        usersDescription.name(callParticipants().others.map { it.userId })

    private fun clearNotification() {
        stopForegroundLocal()
        NotificationManager.cancelNotification(CALL_NOTIFICATION_ID)
    }
}