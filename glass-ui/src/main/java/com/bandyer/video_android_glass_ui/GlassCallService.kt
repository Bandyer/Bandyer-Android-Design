package com.bandyer.video_android_glass_ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bandyer.android_common.audio.CallAudioManager
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.battery_observer.BatteryObserver
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.android_common.network_observer.WiFiObserver
import com.bandyer.collaboration_center.BuddyUser
import com.bandyer.collaboration_center.Collaboration
import com.bandyer.collaboration_center.PhoneBox
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.collaboration_center.phonebox.Input
import com.bandyer.collaboration_center.phonebox.Inputs
import com.bandyer.collaboration_center.phonebox.VideoStreamView
import com.bandyer.video_android_glass_ui.model.Permission
import com.bandyer.video_android_glass_ui.model.Volume
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

@SuppressLint("MissingPermission")
class GlassCallService : CallService(), DefaultLifecycleObserver,
                         Application.ActivityLifecycleCallbacks {

    companion object {
        var NOTIFICATION_ID = 22
    }

    private var collaboration: Collaboration? = null
    private var currentCall: Call? = null
    private var shouldDisconnect = false

    private var fragmentActivity: FragmentActivity? = null
    private var wasVideoEnabledOnDestroy = false

    private var batteryObserver: BatteryObserver? = null
    private var wifiObserver: WiFiObserver? = null

    private var callAudioManager: CallAudioManager? = null

    //    private var ongoingCalls: MutableSet<Call> = mutableSetOf()
    override val call: SharedFlow<Call>
        get() = collaboration!!.phoneBox.call

    override val userDetailsDelegate: StateFlow<UserDetailsDelegate?> =
        MutableStateFlow<UserDetailsDelegate?>(null).apply {
            value =
                userDetailsDelegate {
                    data = listOf(
                        UserDetails(
                            "ste1",
                            "Mario",
                            "Mario",
                            "Rossi",
                            "mario@gmail.com",
                            null,
                            null,
                            null
                        ),
                        UserDetails(
                            "ste2",
                            "Luigi",
                            "Luigi",
                            "Gialli",
                            "luigi@gmail.com",
                            null,
                            "https://randomuser.me/api/portraits/men/86.jpg",
                            null
                        )
                    )
                    defaultFormatter = { userDetails ->
                        if (userDetails.count() > 1) {
                            var text = ""
                            userDetails.forEach { text += "${it.firstName} ${it.lastName}, " }
                            text
                        } else "${userDetails.first().firstName} ${userDetails.first().lastName}"
                    }
                    callFormatter = defaultFormatter
                }

        }

    override val battery: SharedFlow<BatteryInfo>
        get() = batteryObserver!!.observe()

    override val wifi: SharedFlow<WiFiInfo>
        get() = wifiObserver!!.observe()

    // Service
    override fun onCreate() {
        super<CallService>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)
        callAudioManager = CallAudioManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        application.registerActivityLifecycleCallbacks(this)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super<CallService>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(this)

        currentCall?.disconnect()
        collaboration?.phoneBox?.disconnect()
        batteryObserver?.stop()
        wifiObserver?.stop()

        currentCall = null
        collaboration = null
        batteryObserver = null
        wifiObserver = null
        callAudioManager = null
    }

    // DefaultLifecycleObserver
    override fun onStart(owner: LifecycleOwner) {
        shouldDisconnect = false
        if (currentCall != null) return
        collaboration?.phoneBox?.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        shouldDisconnect = true
        if (currentCall != null) return
        collaboration?.phoneBox?.disconnect()
    }

    // ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity !is GlassActivity) return
        fragmentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity !is GlassActivity) return
        val video = currentCall?.participants?.value?.me?.streams?.value?.lastOrNull { it.video.value is Input.Video.Camera }?.video?.value ?: return
        if (wasVideoEnabledOnDestroy) video.tryEnable() else video.tryDisable()
        wasVideoEnabledOnDestroy = false
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) {
        if (activity !is GlassActivity) return
        val video = currentCall?.participants?.value?.me?.streams?.value?.lastOrNull { it.video.value is Input.Video.Camera }?.video?.value ?: return
        wasVideoEnabledOnDestroy = video.enabled.value
        video.tryDisable()
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (activity !is GlassActivity) return
        val video = currentCall?.participants?.value?.me?.streams?.value?.lastOrNull { it.video.value is Input.Video.Camera }?.video?.value ?: return
        wasVideoEnabledOnDestroy = video.enabled.value
        video.tryDisable()
        fragmentActivity = null
    }

    // CallService
    override fun dial(otherUsers: List<String>, withVideoOnStart: Boolean?) {
        collaboration!!.phoneBox.create(otherUsers.map { BuddyUser(it.trim()) }) {
            val video =
                withVideoOnStart?.let { if (it) Call.Video.Enabled else Call.Video.Disabled }
            preferredType = Call.PreferredType(audio = Call.Audio.Enabled, video = video)
        }.connect()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun joinUrl(joinUrl: String) = collaboration!!.phoneBox.create(joinUrl).connect()

    override fun connect(collaboration: Collaboration) {
        this.collaboration = collaboration.apply {
            phoneBox.observe()
            phoneBox.connect()
        }
    }

    override fun disconnect() = collaboration!!.phoneBox.disconnect()

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                "channelId",
                "Kaleyra Call",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return NotificationCompat.Builder(applicationContext, "channelId")
            .setContentIntent(GlassUIProvider.createCallPendingIntent(applicationContext))
            .setSmallIcon(R.drawable.bandyer_z_audio_only)
            .setContentText("Tap to go back to call")
            .setContentTitle("Kaleyra Call")
            .build()
    }

    private fun PhoneBox.observe() {
        call.onEach { call ->
            if (currentCall != null) return@onEach
//                if (ongoingCalls.isNotEmpty()) return@onEach

//                ongoingCalls.add(call)
            currentCall = call
            call.setup()

            GlassUIProvider.showCall(applicationContext)
        }.launchIn(lifecycleScope)
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

                if (shouldDisconnect)
                    collaboration?.phoneBox?.disconnect()

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