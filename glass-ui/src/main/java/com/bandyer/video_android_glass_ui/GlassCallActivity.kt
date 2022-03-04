/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bandyer.video_android_glass_ui

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.kaleyra.collaboration_suite.phonebox.Call
import com.bandyer.video_android_core_ui.call.CallActivity
import com.bandyer.video_android_core_ui.call.CallService
import com.bandyer.video_android_core_ui.call.CallUIController
import com.bandyer.video_android_core_ui.call.CallUIDelegate
import com.bandyer.video_android_core_ui.common.DeviceStatusDelegate
import com.bandyer.video_android_glass_ui.ActivityExtensions.enableImmersiveMode
import com.bandyer.video_android_glass_ui.call.CallEndedFragmentArgs
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.databinding.BandyerActivityGlassBinding
import com.bandyer.video_android_glass_ui.status_bar_views.StatusBarView
import com.bandyer.video_android_glass_ui.utils.GlassGestureDetector
import com.bandyer.video_android_glass_ui.utils.currentNavigationFragment
import com.bandyer.video_android_glass_ui.utils.extensions.ActivityExtensions.turnScreenOnAndKeyguardOff
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

/**
 * GlassCallActivity
 */
internal class GlassCallActivity :
    CallActivity(),
    GlassGestureDetector.OnGestureListener,
    ChatNotificationManager.NotificationListener,
    TouchEventListener {

    private lateinit var binding: BandyerActivityGlassBinding

    private var isActivityInForeground = false

    private var service: CallService? = null
    val isServiceBound: Boolean
        get() = service != null

    private val viewModel: GlassViewModel by viewModels {
        GlassViewModelFactory(
            service as CallUIDelegate,
            service as DeviceStatusDelegate,
            service as CallUIController
        )
    }

    private var itemAdapter: ItemAdapter<StreamItem<*>>? = null
    private var currentStreamItemIndex = 0

    private var navController: NavController? = null

    private var glassGestureDetector: GlassGestureDetector? = null

    private var notificationManager: ChatNotificationManager? = null
    private var isNotificationVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.bandyer_activity_glass)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.bandyer_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        glassGestureDetector = GlassGestureDetector(this, this)

        notificationManager =
            ChatNotificationManager(binding.bandyerContent).also { it.addListener(this) }

        // Set up the streams' recycler view
        with(binding.bandyerStreams) {
            itemAdapter = ItemAdapter()
            val fastAdapter = FastAdapter.with(itemAdapter!!)
            val layoutManager =
                LinearLayoutManager(this@GlassCallActivity, LinearLayoutManager.HORIZONTAL, false)

            this.layoutManager = layoutManager
            adapter = fastAdapter.apply {
                stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }
            isFocusable = false
            setHasFixedSize(true)
        }

        enableImmersiveMode()
        turnScreenOnAndKeyguardOff()
    }

    override fun onServiceBound(service: CallService) {
        this.service = service

        if (intent.extras?.get("autoAnswer") == true)
            viewModel.onAnswer()

        viewModel.onRequestMicPermission(this)
        viewModel.onRequestCameraPermission(this)

        // Add a scroll listener to the recycler view to show mic/cam blocked/disabled toasts
        with(binding.bandyerStreams){
            val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val foundView = snapHelper.findSnapView(layoutManager) ?: return
                    val position = layoutManager!!.getPosition(foundView)

                    if (itemAdapter!!.getAdapterItem(position).streamParticipant.isMyStream && currentStreamItemIndex != position) {
                        val isMicBlocked = viewModel.micPermission.value.let {
                            !it.isAllowed && it.neverAskAgain
                        }
                        val isCamBlocked = viewModel.camPermission.value.let {
                            !it.isAllowed && it.neverAskAgain
                        }
                        val isMicEnabled = viewModel.micEnabled.value
                        val isCameraEnabled = viewModel.cameraEnabled.value

                        when {
                            isMicBlocked && isCamBlocked -> resources.getString(R.string.bandyer_glass_mic_and_cam_blocked)
                            isMicBlocked -> resources.getString(R.string.bandyer_glass_mic_blocked)
                            isCamBlocked -> resources.getString(R.string.bandyer_glass_cam_blocked)
                            else -> null
                        }?.also { binding.bandyerToastContainer.show(BLOCKED_TOAST_ID, it) }

                        when {
                            !isMicBlocked && !isMicEnabled && !isCamBlocked && !isCameraEnabled ->
                                resources.getString(R.string.bandyer_glass_mic_and_cam_not_active)
                            !isMicBlocked && !isMicEnabled ->
                                resources.getString(R.string.bandyer_glass_mic_not_active)
                            !isCamBlocked && !isCameraEnabled ->
                                resources.getString(R.string.bandyer_glass_cam_not_active)
                            else -> null
                        }?.also { binding.bandyerToastContainer.show(DISABLED_TOAST_ID, it) }
                    }

                    currentStreamItemIndex = position
                }
            })
        }

        with(binding.bandyerStatusBar) {
            if (viewModel.call.extras.recording is Call.Recording.OnConnect) showRec() else hideRec()
        }

        viewModel.call.state
            .dropWhile { it == Call.State.Disconnected }
            .takeWhile { it !is Call.State.Disconnected }
            .onCompletion {
                if (!isActivityInForeground) finishAndRemoveTask()
            }.launchIn(lifecycleScope)

        repeatOnStarted {
            viewModel
                .battery
                .onEach {
                    with(binding.bandyerStatusBar) {
                        setBatteryChargingState(it.state == BatteryInfo.State.CHARGING)
                        setBatteryCharge(it.percentage)
                    }
                }
                .launchIn(this)

            viewModel
                .wifi
                .onEach {
                    binding.bandyerStatusBar.setWiFiSignalState(
                        when {
                            it.state == WiFiInfo.State.DISABLED -> StatusBarView.WiFiSignalState.DISABLED
                            it.level == WiFiInfo.Level.NO_SIGNAL || it.level == WiFiInfo.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                            it.level == WiFiInfo.Level.FAIR || it.level == WiFiInfo.Level.GOOD -> StatusBarView.WiFiSignalState.MODERATE
                            else -> StatusBarView.WiFiSignalState.FULL
                        }
                    )
                }
                .launchIn(this)

            viewModel.call.state
                .dropWhile { it == Call.State.Disconnected }
                .onEach {
                    if (it is Call.State.Reconnecting) navController!!.navigate(R.id.reconnectingFragment)
                    if (it is Call.State.Disconnected.Ended) {
                        val title = resources.getString(R.string.bandyer_glass_call_ended)

                        val subtitle = when (it) {
                            is Call.State.Disconnected.Ended.Declined -> resources.getString(R.string.bandyer_glass_call_declined)
                            is Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> resources.getString(
                                R.string.bandyer_glass_answered_on_another_device
                            )
                            is Call.State.Disconnected.Ended.LineBusy -> resources.getString(R.string.bandyer_glass_line_busy)
                            is Call.State.Disconnected.Ended.HangUp -> resources.getString(R.string.bandyer_glass_call_hunged_up)
                            is Call.State.Disconnected.Ended.Error -> resources.getString(R.string.bandyer_glass_call_error_occurred)
                            is Call.State.Disconnected.Ended.Timeout -> resources.getString(R.string.bandyer_glass_call_timeout)
                            else -> null
                        }

                        val navArgs = CallEndedFragmentArgs(title, subtitle).toBundle()
                        navController!!.navigate(R.id.callEndedFragment, navArgs)
                    }
                }.launchIn(this)

            viewModel.amIAlone
                .onEach {
                    with(binding.bandyerToastContainer) {
                        if (it) show(
                            ALONE_TOAST_ID,
                            resources.getString(R.string.bandyer_glass_alone),
                            R.drawable.ic_bandyer_glass_alert,
                            0L
                        )
                        else cancel(ALONE_TOAST_ID)
                    }
                }.launchIn(this)

            viewModel.cameraEnabled
                .onEach {
                    with(binding.bandyerStatusBar) {
                        if (it) hideCamMutedIcon() else showCamMutedIcon()
                    }
                }.launchIn(this)

            viewModel.micEnabled
                .onEach {
                    with(binding.bandyerStatusBar) {
                        if (it) hideMicMutedIcon() else showMicMutedIcon()
                    }
                }.launchIn(this)

            viewModel.micPermission
                .onEach {
                    if (!it.isAllowed && it.neverAskAgain)
                        binding.bandyerStatusBar.showMicMutedIcon(true)
                }
                .launchIn(this)

            viewModel.camPermission
                .onEach {
                    if (!it.isAllowed && it.neverAskAgain)
                        binding.bandyerStatusBar.showCamMutedIcon(true)
                }
                .launchIn(this)

            viewModel.onParticipantJoin
                .onEach { part ->
                    val text = resources.getString(
                        R.string.bandyer_glass_user_joined_pattern,
                        viewModel.usersDescription.name(listOf(part.userAlias))
                    )
                    binding.bandyerToastContainer.show(text = text)
                }.launchIn(this)

            viewModel.onParticipantLeave
                .onEach { part ->
                    val text = resources.getString(
                        R.string.bandyer_glass_user_left_pattern,
                        viewModel.usersDescription.name(listOf(part.userAlias))
                    )
                    binding.bandyerToastContainer.show(text = text)
                }.launchIn(this)

            viewModel.streams
                .onEach { streams ->
                    val orderedList = streams.sortedBy { !it.isMyStream }.map {
                        if (it.isMyStream) MyStreamItem(
                            it,
                            lifecycleScope,
                            viewModel.micPermission,
                            viewModel.camPermission
                        ) else OtherStreamItem(it, lifecycleScope)
                    }
                    FastAdapterDiffUtil[itemAdapter!!] =
                        FastAdapterDiffUtil.calculateDiff(itemAdapter!!, orderedList, true)
                }.launchIn(this)
        }

        if (wasPausedForBackground) {
            viewModel.onEnableCamera(wasPausedForBackground)
            wasPausedForBackground = false
        }
    }

    override fun onStart() {
        super.onStart()
        isActivityInForeground = true
    }

    override fun onStop() {
        super.onStop()
        isActivityInForeground = false
    }

    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        super.onTopResumedActivityChanged(isTopResumedActivity)
        if (!isServiceBound) return

        if (!isTopResumedActivity) wasPausedForBackground = viewModel.cameraEnabled.value
        else if (wasPausedForBackground) {
            viewModel.onEnableCamera(true)
            wasPausedForBackground = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        itemAdapter!!.clear()
        service = null
        itemAdapter = null
        navController = null
        glassGestureDetector = null
        notificationManager = null
    }

    /**
     *  Handle the state bar UI and the notification when the destination fragment
     *  on the nav graph is changed.
     *
     *  NavController.OnDestinationChangedListener is not used because the code
     *  need to be executed when the fragment is actually being created.
     *
     *  @param destinationId The destination fragment's id
     */
    fun onDestinationChanged(destinationId: Int) {
        (destinationId == R.id.chatFragment).also {
            if (it) notificationManager!!.dismiss(false)
            notificationManager!!.dnd = it
        }

        binding.bandyerStatusBar.setBackgroundColor(
            if (fragmentsWithDimmedStatusBar.contains(destinationId))
                ResourcesCompat.getColor(
                    resources,
                    R.color.bandyer_glass_dimmed_background_color,
                    null
                )
            else Color.TRANSPARENT
        )

        binding.bandyerToastContainer.visibility =
            if (destinationId == R.id.emptyFragment) View.VISIBLE else View.GONE
    }

    /**
     * @suppress
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Dispatch the touch event to the gesture detector
        return if (ev != null && glassGestureDetector!!.onTouchEvent(ev)) true
        else super.dispatchTouchEvent(ev)
    }

    /**
     * @suppress
     */
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean =
        if (event?.action == MotionEvent.ACTION_DOWN &&
            handleSmartGlassTouchEvent(TouchEvent.getEvent(event))
        ) true
        else super.dispatchKeyEvent(event)

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean =
        handleSmartGlassTouchEvent(TouchEvent.getEvent(gesture))

    private fun handleSmartGlassTouchEvent(glassEvent: TouchEvent): Boolean =
        if (isNotificationVisible) onTouch(glassEvent)
        else (supportFragmentManager.currentNavigationFragment as? TouchEventListener)?.onTouch(
            glassEvent
        ) ?: false

    override fun onTouch(event: TouchEvent): Boolean =
        when (event.type) {
            TouchEvent.Type.TAP -> true.also { notificationManager!!.expand() }
            TouchEvent.Type.SWIPE_DOWN -> true.also { notificationManager!!.dismiss() }
            else -> false
        }

    override fun onShow() {
        isNotificationVisible = true
    }

    override fun onExpanded() {
        isNotificationVisible = false
        if (supportFragmentManager.currentNavigationFragment?.id != R.id.smartglass_nav_graph_chat)
            navController!!.navigate(R.id.smartglass_nav_graph_chat)
    }

    override fun onDismiss() {
        isNotificationVisible = false
    }

    /**
     * Add a notification listener
     *
     * @param listener NotificationListener
     */
    fun addNotificationListener(listener: ChatNotificationManager.NotificationListener) {
        notificationManager!!.addListener(listener)
    }

    /**
     * Remove a notification listener
     *
     * @param listener NotificationListener
     */
    fun removeNotificationListener(listener: ChatNotificationManager.NotificationListener) {
        notificationManager!!.removeListener(listener)
    }

    private companion object {
        const val BLOCKED_TOAST_ID = "blocked-input"
        const val DISABLED_TOAST_ID = "disabled-input"
        const val ALONE_TOAST_ID = "alone-in-call"
        val fragmentsWithDimmedStatusBar = setOf(
            R.id.dialingFragment,
            R.id.ringingFragment,
            R.id.reconnectingFragment,
            R.id.endCallFragment,
            R.id.callEndedFragment,
            R.id.chatFragment,
            R.id.chatMenuFragment,
            R.id.participantsFragment
        )
        var wasPausedForBackground = false
    }
}