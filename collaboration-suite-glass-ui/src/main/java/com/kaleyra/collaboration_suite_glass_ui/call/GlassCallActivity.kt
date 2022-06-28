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

package com.kaleyra.collaboration_suite_glass_ui.call

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Whiteboard
import com.kaleyra.collaboration_suite.phonebox.Whiteboard.LoadOptions
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.DeviceStatusObserver
import com.kaleyra.collaboration_suite_core_ui.call.CallController
import com.kaleyra.collaboration_suite_core_ui.call.CallDelegate
import com.kaleyra.collaboration_suite_core_ui.call.widget.LivePointerView
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOff
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_glass_ui.GlassBaseActivity
import com.kaleyra.collaboration_suite_glass_ui.GlassTouchEventManager
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.TouchEvent
import com.kaleyra.collaboration_suite_glass_ui.TouchEventListener
import com.kaleyra.collaboration_suite_glass_ui.WhiteboardItem
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.MyStreamItem
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.OtherStreamItem
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.StreamItem
import com.kaleyra.collaboration_suite_glass_ui.call.fragments.CallEndedFragmentArgs
import com.kaleyra.collaboration_suite_glass_ui.common.OnDestinationChangedListener
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraCallActivityGlassBinding
import com.kaleyra.collaboration_suite_glass_ui.model.internal.StreamParticipant
import com.kaleyra.collaboration_suite_glass_ui.status_bar_views.StatusBarView
import com.kaleyra.collaboration_suite_glass_ui.utils.currentNavigationFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ActivityExtensions.enableImmersiveMode
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getCallThemeAttribute
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.collaboration_suite_utils.audio.CallAudioManager
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * GlassCallActivity
 */
internal class GlassCallActivity :
    GlassBaseActivity(),
    OnDestinationChangedListener,
    TouchEventListener,
    GlassTouchEventManager.Listener {

    private lateinit var binding: KaleyraCallActivityGlassBinding

    private val deviceStatusObserver = DeviceStatusObserver()

    private val viewModel: CallViewModel by viewModels {
        CallViewModelFactory(
            CallDelegate(CollaborationUI.phoneBox.call, CollaborationUI.usersDescription),
            CallController(CollaborationUI.phoneBox.call, CallAudioManager(ContextRetainer.context)),
            deviceStatusObserver
        )
    }

    private var isActivityInForeground = false
    private var fastAdapter: FastAdapter<AbstractItem<*>>? = null
    private var streamsItemAdapter: ItemAdapter<StreamItem<*>>? = null
    private var whiteboardItemAdapter: ItemAdapter<WhiteboardItem>? = null
    private var currentStreamItemIndex = 0
    private var streamMutex = Mutex()
    private val hideStreamOverlay = MutableStateFlow(true)

    // The value is a Pair<UserId, ItemIdentifier>
    private val livePointers: ConcurrentMap<LivePointerView, Pair<String, Long>> = ConcurrentHashMap()
    private var navController: NavController? = null
    private var glassTouchEventManager: GlassTouchEventManager? = null

    val rvStreams: RecyclerView get() = binding.kaleyraStreams

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KaleyraCallActivityGlassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.kaleyra_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        glassTouchEventManager = GlassTouchEventManager(this, this)

        // Set up the streams' recycler view
        with(binding.kaleyraStreams) {
            streamsItemAdapter = ItemAdapter()
            whiteboardItemAdapter = ItemAdapter()
            fastAdapter = FastAdapter.with(listOf(whiteboardItemAdapter!!, streamsItemAdapter!!))
            val layoutManager =
                LinearLayoutManager(this@GlassCallActivity, LinearLayoutManager.HORIZONTAL, false)

            this.layoutManager = layoutManager
            adapter = fastAdapter!!
            adapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            isFocusable = false
            setHasFixedSize(true)
        }

        if (DeviceUtils.isSmartGlass) enableImmersiveMode()
        turnScreenOn()

        deviceStatusObserver.start()
        handleIntentAction(intent)
        bindUI()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentAction(intent)
    }

    private fun bindUI() {
        val preferredCallType = viewModel.preferredCallType.value
        if (!viewModel.micPermission.value.isAllowed && preferredCallType?.hasAudio() == true && preferredCallType.isAudioEnabled())
            viewModel.onRequestMicPermission(this)

        if (!viewModel.camPermission.value.isAllowed && preferredCallType?.hasVideo() == true && preferredCallType.isVideoEnabled())
            viewModel.onRequestCameraPermission(this)

        // Add a scroll listener to the recycler view to show mic/cam blocked/disabled toasts
        with(binding.kaleyraStreams) {
            val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var lastView: View? = null

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    binding.kaleyraOuterPointers.visibility =
                        if (newState != SCROLL_STATE_IDLE) View.GONE else View.VISIBLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val foundView = snapHelper.findSnapView(layoutManager) ?: return
                    val position = layoutManager!!.getPosition(foundView)
                    if (lastView != foundView && currentStreamItemIndex != position) {
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
                                isMicBlocked                 -> resources.getString(R.string.kaleyra_glass_mic_blocked)
                                isCamBlocked                 -> resources.getString(R.string.kaleyra_glass_cam_blocked)
                                else                         -> null
                            }?.also { binding.kaleyraToastContainer.show(BLOCKED_TOAST_ID, it) }

                            when {
                                !isMicBlocked && !isMicEnabled && !isCamBlocked && !isCameraEnabled ->
                                    resources.getString(R.string.kaleyra_glass_mic_and_cam_not_active)
                                !isMicBlocked && !isMicEnabled                                      ->
                                    resources.getString(R.string.kaleyra_glass_mic_not_active)
                                !isCamBlocked && !isCameraEnabled                                   ->
                                    resources.getString(R.string.kaleyra_glass_cam_not_active)
                                else                                                                -> null
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
                    }

                    lastView = foundView
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

        if (!viewModel.micPermission.value.isAllowed)
            viewModel.micPermission
                .takeWhile { !it.isAllowed }
                .onCompletion { viewModel.onEnableMic(true) }
                .launchIn(lifecycleScope)

        if (!viewModel.camPermission.value.isAllowed)
            viewModel.camPermission
                .takeWhile { !it.isAllowed }
                .onCompletion { viewModel.onEnableCamera(true) }
                .launchIn(lifecycleScope)

        repeatOnStarted {
            viewModel.areThereNewMessages
                .onEach { binding.kaleyraStatusBar.apply { if (it) showChatIcon() else hideChatIcon() } }
                .launchIn(this)

            viewModel.whiteboard
                .flatMapLatest { it.events }
                .onEach {
                    if (it !is Whiteboard.Event.Request.Show || whiteboardItemAdapter!!.adapterItemCount < 1) return@onEach
                    binding.kaleyraStreams.smoothScrollToPosition(0)
                }.launchIn(this)

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
                            it.state == WiFiInfo.State.DISABLED                                     -> StatusBarView.WiFiSignalState.DISABLED
                            it.level == WiFiInfo.Level.NO_SIGNAL || it.level == WiFiInfo.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                            it.level == WiFiInfo.Level.FAIR || it.level == WiFiInfo.Level.GOOD      -> StatusBarView.WiFiSignalState.MODERATE
                            else                                                                    -> StatusBarView.WiFiSignalState.FULL
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
                            is Call.State.Disconnected.Ended.Declined                -> resources.getString(R.string.kaleyra_glass_call_declined)
                            is Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> resources.getString(
                                R.string.kaleyra_glass_answered_on_another_device
                            )
                            is Call.State.Disconnected.Ended.LineBusy                -> resources.getString(R.string.kaleyra_glass_line_busy)
                            is Call.State.Disconnected.Ended.HangUp                  -> resources.getString(R.string.kaleyra_glass_call_hung_up)
                            is Call.State.Disconnected.Ended.Error                   -> resources.getString(R.string.kaleyra_glass_call_error_occurred)
                            is Call.State.Disconnected.Ended.Timeout                 -> resources.getString(R.string.kaleyra_glass_call_timeout)
                            else                                                     -> null
                        }

                        val navArgs = CallEndedFragmentArgs(title, subtitle).toBundle()
                        navController!!.navigate(R.id.callEndedFragment, navArgs)
                    }
                }.launchIn(this)
            
            viewModel.amIAlone
                .takeWhile { it }
                .onCompletion { binding.kaleyraStatusBar.showTimer() }
                .launchIn(this@repeatOnStarted)

            viewModel.callState
                .takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion { binding.kaleyraStatusBar.hideTimer() }
                .launchIn(this)

            viewModel.amIAlone
                .dropWhile { it }
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

            combine(viewModel.cameraEnabled, viewModel.callState) { enabled, state ->
                if (state != Call.State.Connected) return@combine
                with(binding.kaleyraStatusBar) {
                    if (enabled) hideCamMutedIcon() else showCamMutedIcon()
                }
            }.launchIn(this)

            combine(viewModel.micEnabled, viewModel.callState) { enabled, state ->
                if (state != Call.State.Connected) return@combine
                with(binding.kaleyraStatusBar) {
                    if (enabled) hideMicMutedIcon() else showMicMutedIcon()
                }
            }.launchIn(this)

            combine(viewModel.micPermission, viewModel.callState) { permission, state ->
                if (state != Call.State.Connected || permission.isAllowed || !permission.neverAskAgain) return@combine
                binding.kaleyraStatusBar.showMicMutedIcon(true)
            }.launchIn(this)

            combine(viewModel.camPermission, viewModel.callState) { permission, state ->
                if (state != Call.State.Connected || permission.isAllowed || !permission.neverAskAgain) return@combine
                binding.kaleyraStatusBar.showCamMutedIcon(true)
            }.launchIn(this)

            combine(viewModel.timeToLive, viewModel.duration) { ttl, duration ->
                binding.kaleyraStatusBar.setTimer(ttl ?: duration)
            }.launchIn(this)

            viewModel.timeToLive
                .filter { it != null }
                .onEach {
                    val minutes = (it!! / 60).toInt()
                    when {
                        it == TIMER_BLINK_FOREVER_TH                            -> binding.kaleyraStatusBar.blinkTimer(-1)
                        it % 60 == 0L && ttlWarningThresholds.contains(minutes) -> {
                            val text = resources.getQuantityString(
                                R.plurals.kaleyra_glass_ttl_expiration_pattern,
                                minutes,
                                minutes
                            )
                            binding.kaleyraToastContainer.show(TTL_TOAST_ID, text)
                            binding.kaleyraStatusBar.blinkTimer(TIMER_BLINK_COUNT)
                        }
                    }
                }.launchIn(this)

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
                    val item =
                        streamsItemAdapter!!.adapterItems.firstOrNull { it.streamParticipant.stream.id == streamId }
                            ?: return@onEach
                    livePointers.filterValues { it.second == item.identifier }.keys.firstOrNull()
                        ?.also {
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

            var currentWhiteboard: Whiteboard? = null
            var wbJob: Job? = null
            viewModel.callState
                .takeWhile { it !is Call.State.Connected }
                .onCompletion {
                    combine(viewModel.whiteboard, viewModel.actions) { wb, actions ->
                        if (!actions.any { it is CallUI.Action.OpenWhiteboard }) {
                            if (currentWhiteboard == null) return@combine
                            wbJob?.cancel()
                            currentWhiteboard = null
                            wbJob = null
                        } else {
                            if (currentWhiteboard == wb) return@combine
                            val mode =
                                if (actions.any { it is CallUI.Action.OpenWhiteboard.ViewOnly }) LoadOptions.Mode.ViewOnly else LoadOptions.Mode.Default
                            wbJob?.cancel()
                            wbJob?.join()
//                            currentWhiteboard?.unload()
                            currentWhiteboard = wb
                            wb.load(LoadOptions(mode))
//                            whiteboardItemAdapter!!.clear()
                            whiteboardItemAdapter!!.add(WhiteboardItem(wb))
                            wbJob = viewModel.callState
                                .takeWhile { it !is Call.State.Disconnected.Ended }
                                .onCompletion {
//                                    val linearLayoutManager =
//                                        rvStreams.layoutManager as LinearLayoutManager
//                                    if (linearLayoutManager.findFirstVisibleItemPosition() == 0) return@onCompletion
                                    wb.unload()
                                    whiteboardItemAdapter!!.clear()
                                }
                                .launchIn(this@repeatOnStarted)
                        }
                    }.launchIn(this@repeatOnStarted)
                }
                .launchIn(this)

            // TODO rethink the logic to remove the whiteboard when the user is no longer on it
//            rvStreams.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    if (newState != SCROLL_STATE_IDLE) return
//                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    if (viewModel.actions.value.any { it is CallUI.Action.OpenWhiteboard } || whiteboardItemAdapter?.adapterItemCount == 0 || linearLayoutManager.findFirstVisibleItemPosition() == 0) return
//                    currentWhiteboard?.unload()
//                    whiteboardItemAdapter!!.clear()
//                }
//            })
        }

        if (wasPausedForBackground) {
            viewModel.onEnableCamera(wasPausedForBackground)
            wasPausedForBackground = false
        }
    }

    private fun handleIntentAction(intent: Intent) {
        val action = intent.extras?.getString("action") ?: return
        sendBroadcast(Intent(this, CallNotificationActionReceiver::class.java).apply {
            this.action = action
        })
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
        val itemId =
            streamsItemAdapter!!.adapterItems.firstOrNull { it.streamParticipant.stream.id == streamId }?.identifier
                ?: return
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

        if (!isTopResumedActivity) wasPausedForBackground = viewModel.cameraEnabled.value
        else if (wasPausedForBackground) {
            viewModel.onEnableCamera(true)
            wasPausedForBackground = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOff()
        deviceStatusObserver.stop()
        streamsItemAdapter!!.clear()
        whiteboardItemAdapter!!.clear()
        streamsItemAdapter = null
        whiteboardItemAdapter = null
        navController = null
        glassTouchEventManager = null
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
    override fun onDestinationChanged(destinationId: Int) {
        with(binding.kaleyraStatusBar) {
            setBackgroundColor(
                when {
                    destinationId == R.id.participantsFragment           -> getResourceColor(R.color.kaleyra_glass_background_color)
                    fragmentsWithDimmedStatusBar.contains(destinationId) -> getResourceColor(R.color.kaleyra_glass_dimmed_background_color)
                    else                                                 -> Color.TRANSPARENT
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
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean =
        if (glassTouchEventManager!!.toGlassTouchEvent(ev)) true
        else super.dispatchTouchEvent(ev)

    /**
     * @suppress
     */
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean =
        if (glassTouchEventManager!!.toGlassTouchEvent(event)) true
        else super.dispatchKeyEvent(event)

    override fun onGlassTouchEvent(glassEvent: TouchEvent): Boolean =
        (supportFragmentManager.currentNavigationFragment as? TouchEventListener)?.let {
            if (it.onTouch(glassEvent)) true else onTouch(glassEvent)
        } ?: false

    override fun onTouch(event: TouchEvent): Boolean =
        when {
            event.type == TouchEvent.Type.SWIPE_FORWARD && event.source == TouchEvent.Source.KEY -> true.also {
                binding.kaleyraStreams.horizontalSmoothScrollToNext(currentStreamItemIndex)
            }
            event.type == TouchEvent.Type.SWIPE_BACKWARD && event.source == TouchEvent.Source.KEY -> true.also {
                binding.kaleyraStreams.horizontalSmoothScrollToPrevious(currentStreamItemIndex)
            }
            else                                                                                  -> false
        }

    private companion object {
        const val BLOCKED_TOAST_ID = "blocked-input"
        const val DISABLED_TOAST_ID = "disabled-input"
        const val ALONE_TOAST_ID = "alone-in-call"
        const val TTL_TOAST_ID = "time-to-live-call"
        const val TIMER_BLINK_COUNT = 3
        const val TIMER_BLINK_FOREVER_TH = 30L // seconds
        val ttlWarningThresholds = setOf(5, 2, 1) // minutes
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