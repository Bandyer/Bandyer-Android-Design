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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bandyer.android_common.audio.CallAudioManager
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.battery_observer.BatteryObserver
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.android_common.network_observer.WiFiObserver
import com.bandyer.collaboration_center.BuddyUser
import com.bandyer.collaboration_center.Collaboration
import com.bandyer.collaboration_center.CollaborationSession
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

class GlassCallService : CallService() {

    private companion object {
        var TAG = "${this::class.java}"
        var NOTIFICATION_ID = 2022
    }

    private var collaboration: Collaboration? = null

    private var fragmentActivity: FragmentActivity? = null
    private var activityLifecycleCallback = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
            if (activity is GlassActivity) fragmentActivity = activity
        }

        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) {
            if (activity is GlassActivity) fragmentActivity = null
        }
    }

    private var batteryObserver: BatteryObserver? = null
    private var wifiObserver: WiFiObserver? = null

    private var callAudioManager: CallAudioManager? = null

    //    private var ongoingCalls: MutableSet<Call> = mutableSetOf()
    private var currentCall: Call? = null
    override val call: SharedFlow<Call>
        get() = collaboration!!.phoneBox.call

    private val formatter = object : UserDetailsFormatter {
        override fun format(vararg userDetails: UserDetails): String =
            if (userDetails.count() > 1) {
                var text = ""
                userDetails.forEach { text += "${it.firstName} ${it.lastName}, " }
                text
//                "${userDetails.first().nickName} and other ${userDetails.count() - 1}"
            } else "${userDetails.first().firstName} ${userDetails.first().lastName}"
    }

    private val userDetailsFormatters = UserDetailsFormatters(formatter)
    override val userDetailsWrapper: StateFlow<UserDetailsWrapper> =
        MutableStateFlow(UserDetailsWrapper(listOf(), userDetailsFormatters)).apply {
            value = UserDetailsWrapper(
                listOf(
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
                ),
                userDetailsFormatters
            )
        }

    override val battery: SharedFlow<BatteryInfo>
        get() = batteryObserver!!.observe()

    override val wifi: SharedFlow<WiFiInfo>
        get() = wifiObserver!!.observe()

    override fun onCreate() {
        super.onCreate()
        batteryObserver = BatteryObserver(this)
        @SuppressLint("MissingPermission")
        wifiObserver = WiFiObserver(this)
        callAudioManager = CallAudioManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Get session information
        val session = intent?.getParcelableExtra<CollaborationSession>("session")
        if (session == null) {
            Log.e(TAG, "session is null")
            stopSelf()
            return START_NOT_STICKY
        }

        collaboration = createCollaboration(session)
        collaboration!!.phoneBox.observe()

        // Start observing the activities lifecycle to get GlassActivity's context (needed by addStream)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallback)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallback)
        currentCall?.disconnect()
        collaboration?.phoneBox?.disconnect()
        collaboration?.destroy()
        batteryObserver?.stop()
        wifiObserver?.stop()
        currentCall = null
        collaboration = null
        batteryObserver = null
        wifiObserver = null
        callAudioManager = null
    }

    override fun onDial(otherUsers: List<String>, withVideoOnStart: Boolean?) {
        if (collaboration!!.phoneBox.state.value is PhoneBox.State.Destroyed || collaboration!!.phoneBox.state.value is PhoneBox.State.Failed) {
            Log.e(TAG, "cannot perform call dial")
            return
        }

        collaboration!!.phoneBox.create(otherUsers.map { BuddyUser(it) }) {
            val video =
                withVideoOnStart?.let { if (it) Call.Video.Enabled else Call.Video.Disabled }
            preferredType = Call.PreferredType(audio = Call.Audio.Enabled, video = video)
        }.connect()

        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onUpdateSession(session: CollaborationSession) {
        // TODO Do we want this behaviour when the session in updated?
        currentCall?.disconnect(Call.State.Disconnected.Ended.Error.Client("Session is expired"))
        collaboration!!.phoneBox.disconnect()
        collaboration!!.destroy()
        collaboration = createCollaboration(session)
    }

    private fun createCollaboration(session: CollaborationSession): Collaboration =
        Collaboration.create(session).apply {
            phoneBox.connect()
        }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                "channelId",
                "Bandyer Call",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Bandyer call")
            .build()
    }

    private fun PhoneBox.observe() {
        combine(state, call) { state, call -> Pair(state, call) }
            .takeWhile { it.first !is PhoneBox.State.Destroyed }
            .map { it.second }
            .onEach { call ->
                if (currentCall != null) return@onEach
//                if (ongoingCalls.isNotEmpty()) return@onEach

//                ongoingCalls.add(call)
                currentCall = call
                call.setup()

                GlassUIProvider.showCall(this@GlassCallService)
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
//                ongoingCalls.remove(this@setup)
                stopForeground(true)
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
                video?.view?.value = VideoStreamView(this@GlassCallService)
            }
            .launchIn(lifecycleScope)

    override suspend fun onRequestMicPermission(context: FragmentActivity): Permission {
        return if (currentCall!!.inputs.allowList.value.firstOrNull { it is Input.Audio } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall!!.inputs.request(context, Inputs.Type.Microphone)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }
    }

    override suspend fun onRequestCameraPermission(context: FragmentActivity): Permission {
        return if (currentCall!!.inputs.allowList.value.firstOrNull { it is Input.Video.Camera.Internal } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall!!.inputs.request(context, Inputs.Type.Camera.Internal)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }
    }

    override fun onAnswer() = currentCall!!.connect()

    override fun onHangup() = currentCall!!.disconnect()

    override fun onEnableCamera(enable: Boolean) {
        val video =
            currentCall!!.participants.value.me.streams.value.lastOrNull { it.video.value is Input.Video.Camera }?.video?.value
                ?: return
        if (enable) video.tryEnable() else video.tryDisable()
    }

    override fun onEnableMic(enable: Boolean) {
        val audio =
            currentCall!!.participants.value.me.streams.value.firstOrNull()?.audio?.value ?: return
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