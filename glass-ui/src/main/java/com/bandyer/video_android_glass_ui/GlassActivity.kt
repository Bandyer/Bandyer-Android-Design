package com.bandyer.video_android_glass_ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.video_android_glass_ui.call.CallEndedFragmentArgs
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.databinding.BandyerActivityGlassBinding
import com.bandyer.video_android_glass_ui.status_bar_views.StatusBarView
import com.bandyer.video_android_glass_ui.utils.GlassGestureDetector
import com.bandyer.video_android_glass_ui.utils.currentNavigationFragment
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * GlassActivity
 */
internal class GlassActivity :
    AppCompatActivity(),
    GlassGestureDetector.OnGestureListener,
    ChatNotificationManager.NotificationListener,
    TouchEventListener,
    ServiceBinderActivity {

    private companion object {
        const val BLOCKED_INPUT_TOAST_ID = "input-blocked"
        const val DISABLED_INPUT_TOAST_ID = "input-disabled"
        const val ALONE_TOAST_ID = "blocked-input"
    }

    var service: GlassCallService? = null
    var serviceConnection: ServiceConnection? = null

    // BINDING AND VIEWS
    private lateinit var binding: BandyerActivityGlassBinding
    private var decorView: View? = null

    // SERVICE BINDING
    override var observers = arrayListOf<ServiceBinderActivity.ServiceObserver>()
        private set

    // VIEW MODEL
    private val viewModel: GlassViewModel by viewModels {
        GlassViewModelFactory(
            service as CallUIDelegate,
            service as DeviceStatusDelegate,
            service as CallUIController
        )
    }

    // ACTIVITY FLAG
    private var wasPausedForBackground = false

    // ADAPTER
    private var itemAdapter: ItemAdapter<StreamItem<*>>? = null
    private var snapHelper: LinearSnapHelper? = null
    private var currentStreamItemIndex = 0

    // NAVIGATION
    private val currentFragment: Fragment?
        get() = supportFragmentManager.currentNavigationFragment
    private var navController: NavController? = null

    // GOOGLE GLASS GESTURE DETECTOR
    private var glassGestureDetector: GlassGestureDetector? = null

    // NOTIFICATION
    private var notificationManager: ChatNotificationManager? = null
    private var isNotificationVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterImmersiveMode()

        binding = DataBindingUtil.setContentView(this, R.layout.bandyer_activity_glass)

        // Stream's recyclerView
        with(binding.bandyerStreams) {
            itemAdapter = ItemAdapter()
            val fastAdapter = FastAdapter.with(itemAdapter!!)
            val layoutManager =
                LinearLayoutManager(
                    this@GlassActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

            this.layoutManager = layoutManager
            adapter = fastAdapter.apply {
                stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }
            isFocusable = false
            setHasFixedSize(true)
        }

        // NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.bandyer_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Gesture Detector
        glassGestureDetector = GlassGestureDetector(this, this)

        // Notification Manager
        notificationManager = ChatNotificationManager(binding.bandyerContent).also { it.addListener(this) }
    }

    override fun onStart() {
        super.onStart()
        bindCallService()
    }

    private fun bindCallService() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, binder: IBinder) {
                service = (binder as CallService.ServiceBinder).getService()
                onServiceBound()
                notifyServiceBinding()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                service = null
            }
        }
        Intent(this, GlassCallService::class.java).also { intent ->
            bindService(intent, serviceConnection!!, 0)
        }
    }

    private fun onServiceBound() {
        viewModel.onRequestMicPermission(this)
        viewModel.onRequestCameraPermission(this)

        with(binding.bandyerStreams) {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val foundView = snapHelper!!.findSnapView(layoutManager) ?: return
                    val position = layoutManager!!.getPosition(foundView)
                    if (itemAdapter!!.getAdapterItem(position).streamParticipant.isMyStream && currentStreamItemIndex != position) {
                        with(binding.bandyerToastContainer) {
                            val isMicBlocked =
                                viewModel.micPermission.value.let { !it.isAllowed && it.neverAskAgain }
                            val isCamBlocked =
                                viewModel.camPermission.value.let { !it.isAllowed && it.neverAskAgain }
                            when {
                                isMicBlocked && isCamBlocked -> show(BLOCKED_INPUT_TOAST_ID, resources.getString(R.string.bandyer_glass_mic_and_cam_blocked))
                                isMicBlocked -> show(BLOCKED_INPUT_TOAST_ID, resources.getString(R.string.bandyer_glass_mic_blocked))
                                isCamBlocked -> show(BLOCKED_INPUT_TOAST_ID, resources.getString(R.string.bandyer_glass_cam_blocked))
                            }

                            val isMicEnabled = viewModel.micEnabled.value
                            val isCameraEnabled = viewModel.cameraEnabled.value
                            when {
                                !isMicBlocked && !isMicEnabled && !isCamBlocked && !isCameraEnabled -> show(DISABLED_INPUT_TOAST_ID, resources.getString(R.string.bandyer_glass_mic_and_cam_not_active))
                                !isMicBlocked && !isMicEnabled -> show(DISABLED_INPUT_TOAST_ID, resources.getString(R.string.bandyer_glass_mic_not_active))
                                !isCamBlocked && !isCameraEnabled -> show(DISABLED_INPUT_TOAST_ID, resources.getString(R.string.bandyer_glass_cam_not_active))
                                else -> Unit
                            }
                        }
                    }
                    currentStreamItemIndex = position
                }
            })
        }

        // ServiceObserver events
        repeatOnStarted {
            viewModel
                .battery
                .onEach { binding.bandyerStatusBar.updateBatteryIcon(it) }
                .launchIn(this)

            viewModel
                .wifi
                .onEach { binding.bandyerStatusBar.updateWifiSignalIcon(it) }
                .launchIn(this)

            viewModel.callState
                .dropWhile { it == Call.State.Disconnected }
                .onEach {
                    when (it) {
                        is Call.State.Disconnected.Ended.Declined -> navController!!.navigate(
                            R.id.callEndedFragment,
                            CallEndedFragmentArgs(
                                resources.getString(R.string.bandyer_glass_call_ended),
                                resources.getString(R.string.bandyer_glass_call_declined)
                            ).toBundle()
                        )
                        is Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> navController!!.navigate(
                            R.id.callEndedFragment,
                            CallEndedFragmentArgs(
                                resources.getString(R.string.bandyer_glass_call_ended),
                                resources.getString(R.string.bandyer_glass_answered_on_another_device)
                            ).toBundle()
                        )
                        is Call.State.Disconnected.Ended.LineBusy -> navController!!.navigate(
                            R.id.callEndedFragment,
                            CallEndedFragmentArgs(
                                resources.getString(R.string.bandyer_glass_call_ended),
                                resources.getString(R.string.bandyer_glass_line_busy)
                            ).toBundle()
                        )
                        is Call.State.Disconnected.Ended.HangUp -> navController!!.navigate(
                            R.id.callEndedFragment,
                            CallEndedFragmentArgs(
                                resources.getString(R.string.bandyer_glass_call_ended),
                                resources.getString(R.string.bandyer_glass_call_hunged_up)
                            ).toBundle()
                        )
                        is Call.State.Disconnected.Ended.Error -> navController!!.navigate(
                            R.id.callEndedFragment,
                            CallEndedFragmentArgs(
                                resources.getString(R.string.bandyer_glass_call_ended),
                                resources.getString(R.string.bandyer_glass_call_error_occurred)
                            ).toBundle()
                        )
                        is Call.State.Disconnected.Ended.Timeout -> navController!!.navigate(
                            R.id.callEndedFragment,
                            CallEndedFragmentArgs(
                                resources.getString(R.string.bandyer_glass_call_ended),
                                resources.getString(R.string.bandyer_glass_call_timeout)
                            ).toBundle()
                        )
                        is Call.State.Disconnected.Ended -> navController!!.navigate(
                            R.id.callEndedFragment,
                            CallEndedFragmentArgs(resources.getString(R.string.bandyer_glass_call_ended)).toBundle()
                        )
                        is Call.State.Reconnecting -> navController!!.navigate(R.id.reconnectingFragment)
                        else -> Unit
                    }
                }.launchIn(this)

            viewModel.amIAlone
                .onEach {
                    with(binding) {
                        if (it) bandyerToastContainer.show(
                            ALONE_TOAST_ID,
                            resources.getString(R.string.bandyer_glass_alone),
                            R.drawable.ic_bandyer_glass_alert,
                            0L
                        )
                        else bandyerToastContainer.cancel(ALONE_TOAST_ID)
                    }
                }.launchIn(this)

            viewModel.currentCall
                .onEach {
                    binding.bandyerStatusBar.apply {
                        if (it.extras.recording is Call.Recording.OnConnect) showRec() else hideRec()
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
                    if (!it.isAllowed && it.neverAskAgain) binding.bandyerStatusBar.showMicMutedIcon(
                        true
                    )
                }.launchIn(this)

            viewModel.camPermission
                .onEach {
                    if (!it.isAllowed && it.neverAskAgain) binding.bandyerStatusBar.showCamMutedIcon(
                        true
                    )
                }.launchIn(this)

            viewModel.onParticipantJoin
                .onEach { part ->
                    val userDetailsDelegate = viewModel.userDetailsDelegate.value ?: return@onEach
                    val userDetails =
                        userDetailsDelegate.data!!.firstOrNull { it.userAlias == part.userAlias } ?: UserDetails(part.userAlias)
                    val toastText = resources.getString(
                        R.string.bandyer_glass_user_joined_pattern,
                        userDetails.let {
                            userDetailsDelegate.callFormatter!!.invoke(listOf(userDetails))
                        } ?: part.userAlias)
                    binding.bandyerToastContainer.show(text = toastText)
                }.launchIn(this)

            viewModel.onParticipantLeave
                .onEach { part ->
                    val userDetailsDelegate = viewModel.userDetailsDelegate.value ?: return@onEach
                    val userDetails =
                        userDetailsDelegate.data!!.firstOrNull { it.userAlias == part.userAlias } ?: UserDetails(part.userAlias)
                    val toastText = resources.getString(
                        R.string.bandyer_glass_user_left_pattern,
                        userDetails.let {
                            userDetailsDelegate.callFormatter!!.invoke(listOf(userDetails))
                        } ?: part.userAlias)
                    binding.bandyerToastContainer.show(text = toastText)
                }.launchIn(this)

            viewModel.streams
                .onEach { streams ->
                    val orderedList = streams.sortedBy { !it.isMyStream }.map {
                        if (it.isMyStream) MyStreamItem(
                            it,
                            viewModel.userDetailsDelegate,
                            this,
                            viewModel.micPermission,
                            viewModel.camPermission
                        ) else OtherStreamItem(it, viewModel.userDetailsDelegate, this)
                    }
                    FastAdapterDiffUtil[itemAdapter!!] =
                        FastAdapterDiffUtil.calculateDiff(itemAdapter!!, orderedList, true)
                }.launchIn(this)
        }
    }

    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        super.onTopResumedActivityChanged(isTopResumedActivity)
        if (!isTopResumedActivity) wasPausedForBackground = viewModel.cameraEnabled.value
        else if (wasPausedForBackground) {
            viewModel.onEnableCamera(true)
            wasPausedForBackground = false
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection!!)
        service = null
        serviceConnection = null
    }

    override fun onDestroy() {
        super.onDestroy()
        itemAdapter!!.clear()
        decorView = null
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

        binding.bandyerStatusBar.apply {
            if (setOf(
                    R.id.dialingFragment,
                    R.id.ringingFragment,
                    R.id.reconnectingFragment,
                    R.id.endCallFragment,
                    R.id.callEndedFragment,
                    R.id.chatFragment,
                    R.id.chatMenuFragment,
                    R.id.participantsFragment
                ).contains(destinationId)
            ) applyFlatTint()
            else removeTint()
        }

        binding.bandyerToastContainer.visibility =
            if (destinationId == R.id.emptyFragment) View.VISIBLE else View.GONE
    }

    // GESTURES AND KEYS EVENTS
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
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (event?.action == MotionEvent.ACTION_DOWN && handleSmartGlassTouchEvent(
                TouchEvent.getEvent(
                    event
                )
            )
        ) true
        else super.dispatchKeyEvent(event)
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean =
        handleSmartGlassTouchEvent(TouchEvent.getEvent(gesture))

    private fun handleSmartGlassTouchEvent(glassEvent: TouchEvent): Boolean =
        if (isNotificationVisible) onTouch(glassEvent)
        else (currentFragment as? TouchEventListener)?.onTouch(glassEvent) ?: false

    override fun onTouch(event: TouchEvent): Boolean =
        when (event.type) {
            TouchEvent.Type.TAP -> true.also { notificationManager!!.expand() }
            TouchEvent.Type.SWIPE_DOWN -> true.also { notificationManager!!.dismiss() }
            else -> false
        }

    // NOTIFICATION LISTENER
    override fun onShow() {
        isNotificationVisible = true
    }

    override fun onExpanded() {
        isNotificationVisible = false
        if (currentFragment?.id != R.id.smartglass_nav_graph_chat)
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

    // IMMERSIVE MODE
    private fun enterImmersiveMode() {
        supportActionBar?.hide()
        decorView = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return
        // ATM there is no way of doing this on api > 30
        decorView!!.apply {
            setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                    hideSystemUI()
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            decorView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    // UPDATE STATUS BAR UI
    private fun StatusBarView.applyFlatTint() = setBackgroundColor(
        ResourcesCompat.getColor(
            resources,
            R.color.bandyer_glass_dimmed_background_color,
            null
        )
    )

    private fun StatusBarView.removeTint() = setBackgroundColor(Color.TRANSPARENT)

    private fun StatusBarView.updateCenteredText(nCallParticipants: Int) =
        setCenteredText(
            resources.getQuantityString(
                R.plurals.bandyer_glass_users_in_call_pattern,
                nCallParticipants,
                nCallParticipants
            )
        )

    private fun StatusBarView.updateBatteryIcon(battery: BatteryInfo) {
        setBatteryChargingState(battery.state == BatteryInfo.State.CHARGING)
        setBatteryCharge(battery.percentage)
    }

    private fun StatusBarView.updateWifiSignalIcon(wifi: WiFiInfo) {
        setWiFiSignalState(
            if (wifi.state == WiFiInfo.State.DISABLED) StatusBarView.WiFiSignalState.DISABLED
            else when (wifi.level) {
                WiFiInfo.Level.NO_SIGNAL, WiFiInfo.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                WiFiInfo.Level.FAIR, WiFiInfo.Level.GOOD -> StatusBarView.WiFiSignalState.MODERATE
                WiFiInfo.Level.EXCELLENT -> StatusBarView.WiFiSignalState.FULL
            }
        )
    }
}