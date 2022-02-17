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
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bandyer.android_common.audio.CallAudioManager
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.battery_observer.BatteryObserver
import com.bandyer.android_common.logging.BaseLogger
import com.bandyer.android_common.logging.PriorityLogger
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.android_common.network_observer.WiFiObserver
import com.bandyer.collaboration_center.BuddyUser
import com.bandyer.collaboration_center.Collaboration
import com.bandyer.collaboration_center.CollaborationSession
import com.bandyer.collaboration_center.Configuration
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
import okhttp3.OkHttpClient

@SuppressLint("MissingPermission")
class GlassCallService : CallService(), DefaultLifecycleObserver {

    companion object {
        var TAG = "${this::class.java}"
        var NOTIFICATION_ID = 2022
        var okHttpClient = OkHttpClient.Builder().build()
    }

    private var collaboration: Collaboration? = null

    private var fragmentActivity: FragmentActivity? = null
    private var activityLifecycleCallback = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
            if (activity !is GlassActivity) return
            fragmentActivity = activity
        }

        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) {
            if (activity !is GlassActivity) return
            fragmentActivity = null
        }
    }

    private var hasBeenDisconnected = false

    override fun onStart(owner: LifecycleOwner) {
        if (!hasBeenDisconnected) return
        hasBeenDisconnected = false
        collaboration?.phoneBox?.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        if (currentCall != null) return
        hasBeenDisconnected = true
        collaboration?.phoneBox?.disconnect()
    }

    private var batteryObserver: BatteryObserver? = null
    private var wifiObserver: WiFiObserver? = null

    private var callAudioManager: CallAudioManager? = null

    //    private var ongoingCalls: MutableSet<Call> = mutableSetOf()
    private var currentCall: Call? = null
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

    val isSessionEstablished: Boolean
        get() = collaboration != null && collaboration!!.phoneBox.state.value is PhoneBox.State.Connected

    private var disconnectPhoneBox = false

    override fun onCreate() {
        super<CallService>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)
        callAudioManager = CallAudioManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallback)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return super.onBind(intent)
    }

    override fun onDestroy() {
        super<CallService>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallback)
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

    override fun dial(otherUsers: List<String>, withVideoOnStart: Boolean?) {
        if (collaboration == null) {
            Log.e(TAG, "Collaboration is null")
            return
        }
//        if (collaboration!!.phoneBox.state.value is PhoneBox.State.Destroyed || collaboration!!.phoneBox.state.value is PhoneBox.State.Failed) {
//            Log.e(TAG, "cannot perform call dial")
//            return
//        }

        try {
            collaboration!!.phoneBox.create(otherUsers.map { BuddyUser(it.trim()) }) {
                val video =
                    withVideoOnStart?.let { if (it) Call.Video.Enabled else Call.Video.Disabled }
                preferredType = Call.PreferredType(audio = Call.Audio.Enabled, video = video)
            }.connect()

            startForeground(NOTIFICATION_ID, createNotification())
        } catch (t: Throwable) {
            Log.e(TAG, t.message, t)
        }
    }

    override fun joinUrl(joinUrl: String) {
        collaboration!!.phoneBox.create(joinUrl).connect()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun setupSession(
        session: CollaborationSession,
        onPhoneBoxConnected: (() -> Unit)?,
        onPhoneBoxDisconnected: (() -> Unit)?
    ) {
        // TODO Do we want this behaviour when the session in updated?
        closeSession()
        collaboration = createCollaboration(session) ?: return
        collaboration!!.phoneBox.state
            .onEach {
                when (it) {
                    is PhoneBox.State.Connected    -> onPhoneBoxConnected?.invoke()
                    is PhoneBox.State.Disconnected -> onPhoneBoxDisconnected?.invoke()
                    else                           -> Unit
                }
            }
            .takeWhile {
                it !is PhoneBox.State.Connected
            }
            .launchIn(lifecycleScope)
    }

    override fun closeSession() {
        currentCall?.disconnect(Call.State.Disconnected.Ended.Error.Client("Session closed"))
        collaboration?.phoneBox?.disconnect()
        currentCall = null
        collaboration = null
    }

    override fun connect() {
        if (collaboration == null) {
            Log.e(TAG, "Collaboration is null")
            return
        }
        try {
            if (collaboration!!.phoneBox.state.value is PhoneBox.State.Connected) {
                disconnectPhoneBox = false
                return
            }
            collaboration!!.phoneBox.connect()
        } catch (t: Throwable) {
            Log.e(TAG, t.message, t)
        }
    }

    override fun disconnect(force: Boolean) {
        if (collaboration == null) {
            Log.e(TAG, "Collaboration is null")
            return
        }
        when {
            currentCall == null          -> collaboration?.phoneBox?.disconnect()
            currentCall != null && force -> {
                currentCall?.disconnect(Call.State.Disconnected.Ended.Error.Client("Session closed"))
                collaboration?.phoneBox?.disconnect()
            }
            else                         -> disconnectPhoneBox = true
        }
    }

    private fun createCollaboration(session: CollaborationSession): Collaboration? {
        return try {
            Collaboration.create(session, Configuration(okHttpClient, logger = object : PriorityLogger(BaseLogger.VERBOSE) {
                override val target: Int
                    get() = 1.shl(12) or 1.shl(13) or 1.shl(14) or 1.shl(15) or 1.shl(16)

                override fun verbose(tag: String, message: String) {
                    Log.v(tag, message)
                }

                override fun debug(tag: String, message: String) {
                    Log.d(tag, message)

                }

                override fun info(tag: String, message: String) {
                    Log.i(tag, message)

                }

                override fun warn(tag: String, message: String) {
                    Log.w(tag, message)

                }

                override fun error(tag: String, message: String) {
                    Log.e(tag, message)

                }

            })).apply {
                phoneBox.observe()
                phoneBox.connect()
            }
        } catch (t: Throwable) {
            Log.e(TAG, t.message, t)
            null
        }
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
        val callJob = call.onEach { call ->
            if (currentCall != null) return@onEach
//                if (ongoingCalls.isNotEmpty()) return@onEach

//                ongoingCalls.add(call)
            currentCall = call
            call.setup()

            GlassUIProvider.showCall(this@GlassCallService)
        }.launchIn(lifecycleScope)

        state
            .onCompletion { callJob.cancel() }
            .launchIn(lifecycleScope)
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

                if (disconnectPhoneBox)
                    collaboration?.phoneBox?.disconnect()

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
        return if (currentCall?.inputs?.allowList?.value?.firstOrNull { it is Input.Audio } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall?.inputs?.request(context, Inputs.Type.Microphone)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }
    }

    override suspend fun onRequestCameraPermission(context: FragmentActivity): Permission {
        return if (currentCall?.inputs?.allowList?.value?.firstOrNull { it is Input.Video.Camera.Internal } != null) Permission(
            isAllowed = true,
            neverAskAgain = false
        )
        else currentCall?.inputs?.request(context, Inputs.Type.Camera.Internal)
            .let { Permission(it is Inputs.RequestResult.Allow, it is Inputs.RequestResult.Never) }
    }

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