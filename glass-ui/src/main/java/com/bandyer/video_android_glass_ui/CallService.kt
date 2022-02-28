package com.bandyer.video_android_glass_ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bandyer.android_common.audio.CallAudioManager
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.battery_observer.BatteryObserver
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.android_common.network_observer.WiFiObserver
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.collaboration_center.phonebox.Input
import com.bandyer.collaboration_center.phonebox.Inputs
import com.bandyer.collaboration_center.phonebox.PhoneBox
import com.bandyer.collaboration_center.phonebox.VideoStreamView
import com.bandyer.video_android_core_ui.CallUIController
import com.bandyer.video_android_core_ui.CallUIDelegate
import com.bandyer.video_android_core_ui.DeviceStatusDelegate
import com.bandyer.video_android_core_ui.UsersDescription
import com.bandyer.video_android_core_ui.model.Permission
import com.bandyer.video_android_core_ui.model.Volume
import com.bandyer.video_android_glass_ui.utils.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

abstract class BoundService : LifecycleService() {
    @Suppress("UNCHECKED_CAST")
    inner class ServiceBinder : Binder() {
        fun <T : BoundService> getService(): T = this@BoundService as T
    }

    private var binder: ServiceBinder? = null

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return ServiceBinder().also { binder = it }
    }

    override fun onDestroy() {
        super.onDestroy()
        binder = null
    }
}

@SuppressLint("MissingPermission")
class CallService : BoundService(), CallUIDelegate, CallUIController, DeviceStatusDelegate,
    DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    companion object {
        private const val CALL_NOTIFICATION_ID = 22
        private var currentCall: Call? = null

        fun onNotificationAnswer() = currentCall?.connect()

        fun onNotificationHangUp() = currentCall?.disconnect()
    }

    private var phoneBox: PhoneBox? = null
    private var shouldDisconnect = false
    private var phoneBoxJob: Job? = null

    private var fragmentActivity: FragmentActivity? = null
    private var wasVideoEnabledOnDestroy = false

    private var batteryObserver: BatteryObserver? = null
    private var wifiObserver: WiFiObserver? = null

    private var callAudioManager: CallAudioManager? = null

    //    private var ongoingCalls: MutableSet<Call> = mutableSetOf()
    override val call: SharedFlow<Call>
        get() = phoneBox!!.call

    override var usersDescription: UsersDescription = UsersDescription()

    override val battery: SharedFlow<BatteryInfo>
        get() = batteryObserver!!.observe()

    override val wifi: SharedFlow<WiFiInfo>
        get() = wifiObserver!!.observe()

    // Service
    override fun onCreate() {
        super<BoundService>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)

        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)
        callAudioManager = CallAudioManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super<BoundService>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(this)

        currentCall?.disconnect()
        phoneBox!!.disconnect()
        batteryObserver!!.stop()
        wifiObserver!!.stop()

        currentCall = null
        phoneBox = null
        batteryObserver = null
        wifiObserver = null
        callAudioManager = null
    }

    // DefaultLifecycleObserver
    override fun onStart(owner: LifecycleOwner) {
        shouldDisconnect = false
        if (currentCall != null) return
        phoneBox?.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        shouldDisconnect = true
        if (currentCall != null) return
        phoneBox?.disconnect()
    }

    // ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (!GlassUIProvider.isUIActivity(activity)) return
        fragmentActivity = activity as FragmentActivity
    }

    override fun onActivityStarted(activity: Activity) {
        if (!GlassUIProvider.isUIActivity(activity)) return
        val video =
            currentCall?.participants?.value?.me?.streams?.value?.lastOrNull { it.video.value is Input.Video.Camera }?.video?.value
                ?: return
        if (wasVideoEnabledOnDestroy) video.tryEnable() else video.tryDisable()
        wasVideoEnabledOnDestroy = false
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) {
        if (!GlassUIProvider.isUIActivity(activity)) return
        val video =
            currentCall?.participants?.value?.me?.streams?.value?.lastOrNull { it.video.value is Input.Video.Camera }?.video?.value
                ?: return
        wasVideoEnabledOnDestroy = video.enabled.value
        video.tryDisable()
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (!GlassUIProvider.isUIActivity(activity)) return
        fragmentActivity = null
    }

    // CallService
    fun bind(phoneBox: PhoneBox, usersDescription: UsersDescription? = null) {
        this.phoneBox = phoneBox.apply {
            phoneBoxJob?.cancel()
            phoneBoxJob = observe()
            usersDescription?.also { this@CallService.usersDescription = it }
        }
    }

    private fun PhoneBox.observe(): Job =
        call.onEach { call ->
            if (currentCall != null || call.state.value is Call.State.Disconnected.Ended) return@onEach
//                if (ongoingCalls.isNotEmpty()) return@onEach

//                ongoingCalls.add(call)
            currentCall = call
            call.setup()

            // If it is an ingoing call...
            val participants = call.participants.value
            if (participants.me != participants.creator()) {
                showIncomingNotification()
                return@onEach
            }

            // ...otherwise
            call.state
                .takeWhile { it !is Call.State.Connecting }
                .onCompletion { showCallUI() }
                .launchIn(lifecycleScope)
        }.launchIn(lifecycleScope)

    private fun showIncomingNotification() =
        lifecycleScope.launch {
            val userAliases = currentCall!!.participants.value.others.map { it.userAlias }
            val usersDescription = usersDescription.name(userAliases)
            startForeground(
                CALL_NOTIFICATION_ID,
                NotificationHelper.buildIncomingCallNotification(this@CallService, usersDescription)
            )
        }

    private fun showCallUI() =
        lifecycleScope.launch {
            val userAliases = currentCall!!.participants.value.others.map { it.userAlias }
            val usersDescription = usersDescription.name(userAliases)
            startForeground(
                CALL_NOTIFICATION_ID,
                NotificationHelper.buildOngoingCallNotification(this@CallService, usersDescription)
            )
            GlassUIProvider.showCall(applicationContext)
        }

    private fun Call.setup() {
        val publishJob = publishMySelf()
        val streamsJob = setupStreamsAndVideos()

        state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion {
                publishJob.cancel()
                streamsJob.cancel()
                currentCall = null

                if (shouldDisconnect) phoneBox!!.disconnect()

                stopForeground(true)
//                ongoingCalls.remove(this@setup)
            }.launchIn(lifecycleScope)
    }

    private fun Call.publishMySelf(): Job {
        val hasVideo = extras.preferredType.hasVideo()
        val callInputs = inputs

        return callInputs.allowList.onEach { inputs ->
            if (inputs.isEmpty()) return@onEach

            val videoInput = inputs.lastOrNull { it is Input.Video.My } as? Input.Video.My
            val audioInput = inputs.firstOrNull { it is Input.Audio } as? Input.Audio

            videoInput?.setQuality(Input.Video.Quality.Definition.HD)

            val me = this.participants.value.me

            me.streams.value.firstOrNull { it.id == "main" }?.let {
                it.audio.value = audioInput
                if (hasVideo) it.video.value = videoInput
                return@onEach
            }

            me.addStream(fragmentActivity!!, "main").let {
                it.audio.value = audioInput
                if (hasVideo) it.video.value = videoInput
            }

        }.launchIn(lifecycleScope)
    }

    private fun Call.setupStreamsAndVideos(): Job =
        participants
            .map { it.others + it.me }
            .flatMapLatest { participants -> participants.map { it.streams }.merge() }
            .onEach { streams ->
                streams.forEach { it.open() }
            }
            .flatMapLatest { streams -> streams.map { it.video }.merge() }
            .onEach { video ->
                if (video?.view?.value != null) return@onEach
                video?.view?.value = VideoStreamView(applicationContext)
            }
            .launchIn(lifecycleScope)

    override suspend fun onRequestMicPermission(context: FragmentActivity): Permission =
        if (currentCall?.inputs?.allowList?.value?.firstOrNull { it is Input.Audio } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall?.inputs?.request(context, Inputs.Type.Microphone)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }

    override suspend fun onRequestCameraPermission(context: FragmentActivity): Permission =
        if (currentCall?.inputs?.allowList?.value?.firstOrNull { it is Input.Video.Camera.Internal } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall?.inputs?.request(context, Inputs.Type.Camera.Internal)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }

    override fun onAnswer() {
        currentCall?.connect()
    }

    override fun onHangup() {
        currentCall?.disconnect()
    }

    override fun onEnableCamera(enable: Boolean) {
        val video =
            currentCall?.participants?.value?.me?.streams?.value?.lastOrNull { it.video.value is Input.Video.Camera }?.video?.value
                ?: return
        if (enable) video.tryEnable() else video.tryDisable()
    }

    override fun onEnableMic(enable: Boolean) {
        val audio =
            currentCall?.participants?.value?.me?.streams?.value?.firstOrNull()?.audio?.value
                ?: return
        if (enable) audio.tryEnable() else audio.tryDisable()
    }

    override fun onSwitchCamera() = Unit

    override fun onGetVolume(): Volume = Volume(
        callAudioManager!!.currentVolume,
        callAudioManager!!.minVolume,
        callAudioManager!!.maxVolume
    )

    override fun onSetVolume(value: Int) = callAudioManager!!.setVolume(value)

    override fun onSetZoom(value: Int) = Unit

}