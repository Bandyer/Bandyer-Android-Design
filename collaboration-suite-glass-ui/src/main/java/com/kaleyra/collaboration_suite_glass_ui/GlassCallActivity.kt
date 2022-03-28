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

package com.kaleyra.collaboration_suite_glass_ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.call.CallActivity
import com.kaleyra.collaboration_suite_core_ui.call.CallService
import com.kaleyra.collaboration_suite_core_ui.call.CallUIController
import com.kaleyra.collaboration_suite_core_ui.call.CallUIDelegate
import com.kaleyra.collaboration_suite_core_ui.call.widget.LivePointerView
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOff
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_glass_ui.call.CallEndedFragmentArgs
import com.kaleyra.collaboration_suite_glass_ui.chat.notification.ChatNotificationManager
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraActivityGlassBinding
import com.kaleyra.collaboration_suite_glass_ui.model.internal.StreamParticipant
import com.kaleyra.collaboration_suite_glass_ui.status_bar_views.StatusBarView
import com.kaleyra.collaboration_suite_glass_ui.utils.GlassGestureDetector
import com.kaleyra.collaboration_suite_glass_ui.utils.currentNavigationFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ActivityExtensions.enableImmersiveMode
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getCallThemeAttribute
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * GlassCallActivity
 */
internal class GlassCallActivity :
    CallActivity(),
    GlassGestureDetector.OnGestureListener,
    ChatNotificationManager.NotificationListener,
    TouchEventListener {

    private lateinit var binding: KaleyraActivityGlassBinding

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

    private var fastAdapter: FastAdapter<AbstractItem<*>>? = null
    private var streamsItemAdapter: ItemAdapter<StreamItem<*>>? = null
    private var whiteboardItemAdapter: ItemAdapter<WhiteboardItem>? = null
    private var currentStreamItemIndex = 0
    private var streamMutex = Mutex()

    private val hideStreamOverlay = MutableStateFlow(true)

    // The value is a Pair<UserId, ItemIdentifier>
    private val livePointers: ConcurrentMap<LivePointerView, Pair<String, Long>> =
        ConcurrentHashMap()

    private var navController: NavController? = null

    private var glassGestureDetector: GlassGestureDetector? = null

    private var notificationManager: ChatNotificationManager? = null
    private var isNotificationVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.kaleyra_activity_glass)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.kaleyra_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        glassGestureDetector = GlassGestureDetector(this, this)

        notificationManager =
            ChatNotificationManager(binding.kaleyraContent).also { it.addListener(this) }

        // Set up the streams' recycler view
        with(binding.kaleyraStreams) {
            streamsItemAdapter = ItemAdapter()
            whiteboardItemAdapter = ItemAdapter()
            fastAdapter = FastAdapter.with(listOf(whiteboardItemAdapter!!, streamsItemAdapter!!))
            val layoutManager =
                LinearLayoutManager(this@GlassCallActivity, LinearLayoutManager.HORIZONTAL, false)

            this.layoutManager = layoutManager
            adapter = fastAdapter!!.apply {
                stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }
            isFocusable = false
            setHasFixedSize(true)

            whiteboardItemAdapter!!.add(WhiteboardItem())
        }

        enableImmersiveMode()
        turnScreenOn()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.extras?.get("autoAnswer") == true) viewModel.onAnswer()
    }

    override fun onServiceBound(service: CallService) {
        this.service = service

        if (intent.extras?.get("autoAnswer") == true)
            viewModel.onAnswer()

        val preferredType = viewModel.call.replayCache.last().extras.preferredType
        if (preferredType.hasAudio() && preferredType.isAudioEnabled())
            viewModel.onRequestMicPermission(this)

        if (preferredType.hasVideo() && preferredType.isVideoEnabled())
            viewModel.onRequestCameraPermission(this)

        // Add a scroll listener to the recycler view to show mic/cam blocked/disabled toasts
        with(binding.kaleyraStreams) {
            val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    binding.kaleyraOuterPointers.visibility =
                        if (newState != SCROLL_STATE_IDLE) View.GONE else View.VISIBLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val foundView = snapHelper.findSnapView(layoutManager) ?: return
                    val position = layoutManager!!.getPosition(foundView)
                    if (currentStreamItemIndex == position) return

                    val currentItem = fastAdapter!!.getItem(position) ?: return

                    if ((currentItem as? StreamItem?)?.streamParticipant?.itsMe == true) {
                        val isMicBlocked = viewModel.micPermission.value.let {
                            !it.isAllowed && it.neverAskAgain
                        }
                        val isCamBlocked = viewModel.camPermission.value.let {
                            !it.isAllowed && it.neverAskAgain
                        }
                        val isMicEnabled = viewModel.micEnabled.value
                        val isCameraEnabled = viewModel.cameraEnabled.value

                        when {
                            isMicBlocked && isCamBlocked -> resources.getString(R.string.kaleyra_glass_mic_and_cam_blocked)
                            isMicBlocked -> resources.getString(R.string.kaleyra_glass_mic_blocked)
                            isCamBlocked -> resources.getString(R.string.kaleyra_glass_cam_blocked)
                            else -> null
                        }?.also { binding.kaleyraToastContainer.show(BLOCKED_TOAST_ID, it) }

                        when {
                            !isMicBlocked && !isMicEnabled && !isCamBlocked && !isCameraEnabled ->
                                resources.getString(R.string.kaleyra_glass_mic_and_cam_not_active)
                            !isMicBlocked && !isMicEnabled ->
                                resources.getString(R.string.kaleyra_glass_mic_not_active)
                            !isCamBlocked && !isCameraEnabled ->
                                resources.getString(R.string.kaleyra_glass_cam_not_active)
                            else -> null
                        }?.also { binding.kaleyraToastContainer.show(DISABLED_TOAST_ID, it) }
                    }

                    val previousItem = fastAdapter!!.getItem(currentStreamItemIndex)
                    previousItem?.also { item ->
                        val currentVideoPosition = fastAdapter!!.getPosition(currentItem.identifier)
                        val previousVideoPosition = fastAdapter!!.getPosition(item.identifier)

                        livePointers.filterValues { it.second == item.identifier }.keys.forEach {
                            it.updateLivePointerHorizontalPosition(
                                if (currentVideoPosition > previousVideoPosition) 0f else 100f,
                                enableAutoHide = false,
                                adjustTextOnEdge = true
                            )
                        }
                    }

                    val streamLivePointers =
                        livePointers.filterValues { it.second == currentItem.identifier }.keys
                    streamLivePointers.forEach { it.visibility = View.GONE }
                    val otherStreamsLivePointers = livePointers.keys - streamLivePointers
                    otherStreamsLivePointers.forEach { it.visibility = View.VISIBLE }

                    currentStreamItemIndex = position
                }
            })
        }

        viewModel.callState
            .dropWhile { it == Call.State.Disconnected }
            .takeWhile { it !is Call.State.Disconnected }
            .onCompletion {
                if (!isActivityInForeground) finishAndRemoveTask()
            }.launchIn(lifecycleScope)

        repeatOnStarted {
            viewModel
                .battery
                .onEach {
                    with(binding.kaleyraStatusBar) {
                        setBatteryChargingState(it.state == BatteryInfo.State.CHARGING)
                        setBatteryCharge(it.percentage)
                    }
                }
                .launchIn(this)

            viewModel
                .wifi
                .onEach {
                    binding.kaleyraStatusBar.setWiFiSignalState(
                        when {
                            it.state == WiFiInfo.State.DISABLED -> StatusBarView.WiFiSignalState.DISABLED
                            it.level == WiFiInfo.Level.NO_SIGNAL || it.level == WiFiInfo.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                            it.level == WiFiInfo.Level.FAIR || it.level == WiFiInfo.Level.GOOD -> StatusBarView.WiFiSignalState.MODERATE
                            else -> StatusBarView.WiFiSignalState.FULL
                        }
                    )
                }
                .launchIn(this)

            viewModel.callState
                .dropWhile { it == Call.State.Disconnected }
                .onEach {
                    if (it is Call.State.Reconnecting) navController!!.navigate(R.id.reconnectingFragment)
                    if (it is Call.State.Disconnected.Ended) {
                        val title = resources.getString(R.string.kaleyra_glass_call_ended)

                        val subtitle = when (it) {
                            is Call.State.Disconnected.Ended.Declined -> resources.getString(R.string.kaleyra_glass_call_declined)
                            is Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> resources.getString(
                                R.string.kaleyra_glass_answered_on_another_device
                            )
                            is Call.State.Disconnected.Ended.LineBusy -> resources.getString(R.string.kaleyra_glass_line_busy)
                            is Call.State.Disconnected.Ended.HangUp -> resources.getString(R.string.kaleyra_glass_call_hunged_up)
                            is Call.State.Disconnected.Ended.Error -> resources.getString(R.string.kaleyra_glass_call_error_occurred)
                            is Call.State.Disconnected.Ended.Timeout -> resources.getString(R.string.kaleyra_glass_call_timeout)
                            else -> null
                        }

                        val navArgs = CallEndedFragmentArgs(title, subtitle).toBundle()
                        navController!!.navigate(R.id.callEndedFragment, navArgs)
                    }
                }.launchIn(this)

            viewModel.amIAlone
                .onEach {
                    with(binding.kaleyraToastContainer) {
                        if (it) show(
                            ALONE_TOAST_ID,
                            resources.getString(R.string.kaleyra_glass_alone),
                            R.drawable.ic_kaleyra_glass_alert,
                            0L
                        )
                        else cancel(ALONE_TOAST_ID)
                    }
                }.launchIn(this)

            viewModel.cameraEnabled
                .onEach {
                    with(binding.kaleyraStatusBar) {
                        if (it) hideCamMutedIcon() else showCamMutedIcon()
                    }
                }.launchIn(this)

            viewModel.micEnabled
                .onEach {
                    with(binding.kaleyraStatusBar) {
                        if (it) hideMicMutedIcon() else showMicMutedIcon()
                    }
                }.launchIn(this)

            viewModel.micPermission
                .onEach {
                    if (!it.isAllowed && it.neverAskAgain)
                        binding.kaleyraStatusBar.showMicMutedIcon(true)
                    else if (it.isAllowed) viewModel.onEnableMic(true)
                }
                .launchIn(this)

            viewModel.camPermission
                .onEach {
                    if (!it.isAllowed && it.neverAskAgain)
                        binding.kaleyraStatusBar.showCamMutedIcon(true)
                    else if (it.isAllowed) viewModel.onEnableCamera(true)
                }
                .launchIn(this)

            viewModel.onParticipantJoin
                .onEach { part ->
                    val text = resources.getString(
                        R.string.kaleyra_glass_user_joined_pattern,
                        viewModel.usersDescription.name(listOf(part.userId))
                    )
                    binding.kaleyraToastContainer.show(text = text)
                }.launchIn(this)

            viewModel.onParticipantLeave
                .onEach { part ->
                    val text = resources.getString(
                        R.string.kaleyra_glass_user_left_pattern,
                        viewModel.usersDescription.name(listOf(part.userId))
                    )
                    binding.kaleyraToastContainer.show(text = text)

                    livePointers.filterValues { it.first == part.userId }.keys.firstOrNull()?.also {
                        binding.kaleyraOuterPointers.removeView(it)
                        livePointers.remove(it)
                    }
                }.launchIn(this)

            viewModel.inCallParticipants
                .onEach {
                    binding.kaleyraStatusBar.setCenteredText(
                        resources.getQuantityString(
                            R.plurals.kaleyra_glass_users_in_call_pattern,
                            it.count(),
                            it.count()
                        )
                    )
                }.launchIn(this)

            viewModel.call.onEach {
                with(binding.kaleyraStatusBar) {
                    if (it.extras.recording is Call.Recording.OnConnect) showRec() else hideRec()
                }
            }.launchIn(this)

            val spJobs = mutableListOf<Job>()
            viewModel.streams
                .onEach onEachStreams@{ streams ->
                    spJobs.forEach {
                        it.cancel()
                        it.join()
                    }
                    spJobs.clear()

                    streams.forEach { sp ->
                        spJobs += sp.stream.video.onEach onEachVideo@{ video ->
                            val sortedStreams = streams.sortedWith(
                                compareBy(
                                    { it.stream.video.value !is Input.Video.Screen },
                                    { !it.itsMe })
                            )
                            streamMutex.withLock {
                                FastAdapterDiffUtil.setDiffItems(
                                    streamsItemAdapter!!,
                                    sortedStreams.mapToStreamItem()
                                )
                            }

                            if (video !is Input.Video.Screen) return@onEachVideo
                            binding.kaleyraStreams.smoothScrollToPosition(
                                fastAdapter!!.getPosition(sp.hashCode().toLong())
                            )
                        }.launchIn(this)
                    }
                }.launchIn(this)

            viewModel.removedStreams
                .onEach { streamId ->
                    val item = streamsItemAdapter!!.adapterItems.firstOrNull { it.streamParticipant.stream.id == streamId } ?: return@onEach
                    livePointers.filterValues { it.second == item.identifier }.keys.firstOrNull()?.also {
                        binding.kaleyraOuterPointers.removeView(it)
                        livePointers.remove(it)
                    }
                }.launchIn(this)

            viewModel.livePointerEvents
                .onEach { pair ->
                    val streamId = pair.first
                    val event = pair.second
                    val userId = event.producer.userId

                    onPointerEvent(
                        streamId,
                        event,
                        viewModel.usersDescription.name(listOf(userId))
                    )
                }.launchIn(this)
        }

        if (wasPausedForBackground) {
            viewModel.onEnableCamera(wasPausedForBackground)
            wasPausedForBackground = false
        }
    }

    private fun FastAdapterDiffUtil.setDiffItems(
        itemAdapter: ItemAdapter<StreamItem<*>>,
        items: List<StreamItem<*>>
    ) {
        this[itemAdapter] = calculateDiff(itemAdapter, items, true)
    }

    private fun List<StreamParticipant>.mapToStreamItem() =
        map {
            if (it.itsMe)
                MyStreamItem(
                    it,
                    lifecycleScope,
                    hideStreamOverlay,
                    viewModel.micPermission,
                    viewModel.camPermission
                )
            else
                OtherStreamItem(it, lifecycleScope, hideStreamOverlay)
        }

    private fun onPointerEvent(
        streamId: String,
        event: Input.Video.Event.Pointer,
        userDescription: String
    ) {
        val userId = event.producer.userId
        val livePointer = livePointers.filterValues { it.first == userId }.keys.firstOrNull()

        if (event.action is Input.Video.Event.Action.Idle) {
            livePointer?.also {
                binding.kaleyraOuterPointers.removeView(it)
                livePointers.remove(it)
            }
            return
        }

        val currentItemId = fastAdapter!!.getItem(currentStreamItemIndex)?.identifier ?: return
        val itemId = streamsItemAdapter!!.adapterItems.firstOrNull { it.streamParticipant.stream.id == streamId }?.identifier ?: return
        val livePointerView =
            livePointer ?: LivePointerView(
                ContextThemeWrapper(
                    this@GlassCallActivity,
                    this@GlassCallActivity.getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Glass_Call_kaleyra_livePointerStyle)
                )
            ).also {
                it.id = View.generateViewId()
                it.visibility = if (currentItemId == itemId) View.GONE else View.VISIBLE
                livePointers[it] = Pair(userId, itemId)
                binding.kaleyraOuterPointers.addView(it)
            }

        val currentVideoPosition = fastAdapter!!.getPosition(currentItemId)
        val eventVideoPosition = fastAdapter!!.getPosition(itemId)

        livePointerView.updateLabelText(userDescription)
        livePointerView.updateLivePointerPosition(
            if (currentVideoPosition > eventVideoPosition) 0f else 100f,
            event.position.y,
            enableAutoHide = false,
            adjustTextOnEdge = true
        )
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
        turnScreenOff()
        streamsItemAdapter!!.clear()
        whiteboardItemAdapter!!.clear()
        service = null
        streamsItemAdapter = null
        whiteboardItemAdapter = null
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

        with(binding.kaleyraStatusBar) {
            setBackgroundColor(
                when {
                    destinationId == R.id.participantsFragment -> getResourceColor(R.color.kaleyra_glass_background_color)
                    fragmentsWithDimmedStatusBar.contains(destinationId) -> getResourceColor(R.color.kaleyra_glass_dimmed_background_color)
                    else -> Color.TRANSPARENT
                }
            )
            if (fragmentsWithParticipantsNumber.contains(destinationId)) showCenteredTitle()
            else hideCenteredTitle()
        }

        binding.kaleyraToastContainer.visibility =
            if (destinationId == R.id.emptyFragment) View.VISIBLE else View.GONE

        hideStreamOverlay.value = fragmentsWithNoStreamOverlay.contains(destinationId)
    }

    private fun getResourceColor(@ColorRes color: Int) =
        ResourcesCompat.getColor(resources, color, null)

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
            R.id.chatMenuFragment
        )
        val fragmentsWithParticipantsNumber = setOf(
            R.id.emptyFragment,
            R.id.menuFragment,
            R.id.participantsFragment,
            R.id.zoomFragment,
            R.id.volumeFragment
        )
        val fragmentsWithNoStreamOverlay = setOf(
            R.id.ringingFragment,
            R.id.dialingFragment,
            R.id.endCallFragment,
            R.id.callEndedFragment,
            R.id.reconnectingFragment
        )
        var wasPausedForBackground = false
    }
}