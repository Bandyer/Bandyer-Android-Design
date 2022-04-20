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
import com.kaleyra.collaboration_suite_utils.audio.CallAudioManager
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryObserver
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiObserver
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class CollaborationService : BoundService(),
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

    private var mCallAudioManager: CallAudioManager? = null
    private var mBatteryObserver: BatteryObserver? = null
    private var mWifiObserver: WiFiObserver? = null
    private var mCallActivityClazz: Class<*>? = null
    private var mIsServiceInForeground: Boolean = false

    override val call: SharedFlow<Call>
        get() = TODO("Not yet implemented")
    override val usersDescription: UsersDescription = UsersDescription()

    override var isAppInForeground: Boolean = false

    override val battery: SharedFlow<BatteryInfo> get() = mBatteryObserver!!.observe()
    override val wifi: SharedFlow<WiFiInfo> get() = mWifiObserver!!.observe()

    override val callAudioManager: CallAudioManager get() = mCallAudioManager!!

    override val currentCall: Call
        get() = TODO("Not yet implemented")

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
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super<BoundService>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(this)
        clearNotification()
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
        phoneBoxJob = setUpCallStreams(phoneBox)
    }

    //////////////////////////////
    // DefaultLifecycleObserver //
    //////////////////////////////
    override fun onStart(owner: LifecycleOwner) {
        isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isAppInForeground = false
    }

    ////////////////////////////////////////////
    // Application.ActivityLifecycleCallbacks //
    ////////////////////////////////////////////
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.javaClass != mCallActivityClazz) return
        publishMyStream(activity as FragmentActivity, currentCall)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity.javaClass != mCallActivityClazz || mIsServiceInForeground) return
        lifecycleScope.launch {
            moveNotificationToForeground(
                currentCall,
                usersDescription,
                mCallActivityClazz!!
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

    //////////////////////////////
    // CallNotificationDelegate //
    //////////////////////////////
    override fun showNotification(notification: Notification, showInForeground: Boolean) {
        if (showInForeground) {
            startForeground(CALL_NOTIFICATION_ID, notification).also {
                mIsServiceInForeground = true
            }
        } else NotificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    override fun clearNotification() {
        stopForeground(true).also { mIsServiceInForeground = false }
        NotificationManager.cancelNotification(CALL_NOTIFICATION_ID)
    }
}