package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bandyer.android_common.audio.CallAudioManager
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.battery_observer.BatteryObserver
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.android_common.network_observer.WiFiObserver
import com.bandyer.collaboration_center.BuddyUser
import com.bandyer.collaboration_center.Collaboration
import com.bandyer.collaboration_center.CollaborationSession
import com.bandyer.collaboration_center.CollaborationToken
import com.bandyer.collaboration_center.PhoneBox
import com.bandyer.collaboration_center.SessionUser
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.collaboration_center.phonebox.CallParticipant
import com.bandyer.collaboration_center.phonebox.Input
import com.bandyer.collaboration_center.phonebox.Inputs
import com.bandyer.collaboration_center.phonebox.Stream
import com.bandyer.collaboration_center.phonebox.VideoStreamView
import com.bandyer.video_android_glass_ui.model.Permission
import com.bandyer.video_android_glass_ui.model.Volume
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.plus

class GlassCallService : LifecycleService(), CallUIDelegate, CallUIController, DeviceStatusDelegate {

    companion object {
        private var TAG = "${this::class.java}"
        var instance: GlassCallService? = null
    }

    private var collaboration: Collaboration? = null

    private var context: FragmentActivity? = null
    private var activityLifecycleCallback = object: Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
            if (p0 is GlassActivity) context = p0
        }

        override fun onActivityStarted(p0: Activity) = Unit

        override fun onActivityResumed(p0: Activity) = Unit

        override fun onActivityPaused(p0: Activity) = Unit

        override fun onActivityStopped(p0: Activity) = Unit

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit

        override fun onActivityDestroyed(p0: Activity) {
            if (p0 is GlassActivity) context = null
        }

    }

    private var batteryObserver: BatteryObserver? = null
    private var wifiObserver: WiFiObserver? = null

    private var callAudioManager: CallAudioManager? = null

    private var currentCall: Call? = null
    override val call: Call
        get() = currentCall!!

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
        wifiObserver = WiFiObserver(this)
        callAudioManager = CallAudioManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (instance != null) {
            Log.e(TAG, "Instance is not null!")
            return START_NOT_STICKY
        }
        instance = this

        val session = intent?.getParcelableExtra<CollaborationSession>("session")

        if (session == null) {
            Log.e(TAG, "userAlias or token is null")
            stopSelf()
            return START_NOT_STICKY
        }

        collaboration = Collaboration.create(session).apply {
            phoneBox.connect()
        }

        application.registerActivityLifecycleCallbacks(activityLifecycleCallback)

        collaboration!!.phoneBox.call.observe()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallback)
        instance = null
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

    fun dial(otherUsers: List<String>) {
        if (collaboration!!.phoneBox.state.value !is PhoneBox.State.Connected) {
            Log.e(TAG, "Phone box is not connected")
            return
        }
        collaboration!!.phoneBox.create(otherUsers.map { BuddyUser(it) }) {
//            preferredType = Call.PreferredType(audio = Call.Audio.Enabled, video = Call.Video.Disabled)
        }.connect()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel("channelId", "Bandyer Call", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Bandyer call")
            .build()

        startForeground(999, notification)

//        collaboration!!.phoneBox.state.onEach { state ->
//            if (state !is PhoneBox.State.Connected) return@onEach
//        }.launchIn(lifecycleScope)
    }

    fun updateSession(session: CollaborationSession) {

    }

    private fun SharedFlow<Call>.observe(): Job =
        this.onEach { call ->
            if (currentCall != null) return@onEach

            currentCall = call

            call.participants
                .map { listOf(it.me) }
                .forEachParticipant(
                    lifecycleScope + CoroutineName("ForEachMyStreams"),
                    forEachStream = { it.open() },
                    forEachVideo = {
                        it?.view?.value = VideoStreamView.create(this@GlassCallService)
                    }
                ).launchIn(lifecycleScope)

            call.participants
                .map { it.others }
                .forEachParticipant(
                    lifecycleScope + CoroutineName("ForEachOthersStreams"),
                    forEachStream = { it.open() },
                    forEachVideo = {
                        it?.view?.value = VideoStreamView.create(this@GlassCallService)
                    }
                ).launchIn(lifecycleScope)

            call.state
                .takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion { currentCall = null }
                .launchIn(lifecycleScope)

            GlassUIProvider.showCall(
                this@GlassCallService.applicationContext,
                this@GlassCallService,
                this@GlassCallService,
                this@GlassCallService
            )

            call.publishMySelf()
        }.launchIn(lifecycleScope)

    private fun Call.publishMySelf() {
        val hasVideo = this.extras.preferredType.hasVideo()
        val callInputs = this.inputs

        callInputs.allowList.onEach { inputs ->
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

            me.addStream(context!!, "main").let {
                it.audio.value = audioInput
                if (hasVideo) it.video.value = videoInput
            }

        }.launchIn(lifecycleScope)
    }

    private inline fun Flow<List<CallParticipant>>.forEachParticipant(
        scope: CoroutineScope,
        crossinline forEachStream: (stream: Stream) -> Unit,
        crossinline forEachVideo: (video: Input.Video?) -> Unit
    ): Flow<List<CallParticipant>> {
        val pJobs = mutableListOf<Job>()
        return onEach { participants ->
            pJobs.forEach {
                it.cancel()
                it.join()
            }
            pJobs.clear()
            participants.forEach { participant ->
                val sJobs = mutableListOf<Job>()
                participant.streams.onEach { streams ->
                    sJobs.forEach {
                        it.cancel()
                        it.join()
                    }
                    sJobs.clear()
                    streams.forEach { stream ->
                        forEachStream(stream)
                        sJobs += stream.video.onEach { forEachVideo(it) }.launchIn(scope)
                    }
                }.launchIn(scope)
            }
        }
    }

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