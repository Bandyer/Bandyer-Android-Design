package com.bandyer.video_android_glass_ui

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bandyer.video_android_glass_ui.call.DialingFragment
import com.bandyer.video_android_glass_ui.call.DialingFragmentDirections
import com.bandyer.video_android_glass_ui.call.RingingFragmentDirections
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.databinding.BandyerActivityGlassBinding
import com.bandyer.video_android_glass_ui.status_bar_views.StatusBarView
import com.bandyer.video_android_glass_ui.utils.GlassGestureDetector
import com.bandyer.video_android_glass_ui.utils.currentNavigationFragment
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.bandyer.video_android_glass_ui.utils.observers.battery.BatteryInfo
import com.bandyer.video_android_glass_ui.utils.observers.battery.BatteryObserver
import com.bandyer.video_android_glass_ui.utils.observers.network.WiFiInfo
import com.bandyer.video_android_glass_ui.utils.observers.network.WiFiObserver
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.*

/**
 * GlassActivity
 */
class GlassActivity :
    AppCompatActivity(),
    GlassGestureDetector.OnGestureListener,
    ChatNotificationManager.NotificationListener,
    TouchEventListener {

    // BINDING AND VIEWS
    private var _binding: BandyerActivityGlassBinding? = null
    private val binding: BandyerActivityGlassBinding get() = _binding!!
    private var decorView: View? = null

    // VIEW MODEL
    private val viewModel: GlassViewModel by viewModels { GlassViewModelFactory }

    // ADAPTER
    private var itemAdapter: ItemAdapter<StreamItem>? = null

    // NAVIGATION
    private val currentFragment: Fragment?
        get() = supportFragmentManager.currentNavigationFragment
    private lateinit var navController: NavController

    // GOOGLE GLASS GESTURE DETECTOR
    private lateinit var glassGestureDetector: GlassGestureDetector

    // NOTIFICATION
    private lateinit var notificationManager: ChatNotificationManager
    private var isNotificationVisible = false

    // OBSERVER STATUS BAR UI
    private lateinit var batteryObserver: BatteryObserver
    private lateinit var wifiObserver: WiFiObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.tiltEnabled = intent.extras?.getBoolean("tiltEnabled") ?: false

        _binding = DataBindingUtil.setContentView(this, R.layout.bandyer_activity_glass)

        enterImmersiveMode()

        with(binding.bandyerStreams) {
            itemAdapter = ItemAdapter()
            val fastAdapter = FastAdapter.with(itemAdapter!!)
            val layoutManager = LinearLayoutManager(this@GlassActivity, LinearLayoutManager.HORIZONTAL, false)
            val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

            this.layoutManager = layoutManager
            adapter = fastAdapter.apply { stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY }
            isFocusable = false
            setHasFixedSize(true)
        }

        // NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.bandyer_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Gesture Detector
        glassGestureDetector = GlassGestureDetector(this, this)

        // Notification Manager
        notificationManager =
            ChatNotificationManager(binding.bandyerContent).also { it.addListener(this) }

        // Battery observer
        batteryObserver = BatteryObserver(this)

        // WiFi observer
        wifiObserver = WiFiObserver(this)

        // Observer events
        repeatOnStarted {
            batteryObserver.observe().onEach { binding.bandyerStatusBar.updateBatteryIcon(it) }.launchIn(this)

            wifiObserver.observe().onEach { binding.bandyerStatusBar.updateWifiSignalIcon(it) }.launchIn(this)

            viewModel.call
                .flatMapConcat { call -> call.state.combine(call.participants) { state, participants -> Pair(state, participants) } }
                .onEach {
                    val state = it.first
                    val participants = it.second
                    when {
                        state is Call.State.Connecting && participants.me == participants.creator -> navController.safeNavigate(StartFragmentDirections.actionStartFragmentToDialingFragment())
                        state is Call.State.Connecting -> navController.safeNavigate(StartFragmentDirections.actionStartFragmentToRingingFragment())
                        state is Call.State.Connected -> {
                            val destination = if (currentFragment is DialingFragment) DialingFragmentDirections.actionDialingFragmentToEmptyFragment() else RingingFragmentDirections.actionRingingFragmentToEmptyFragment()
                            navController.safeNavigate(destination)
                        }
                        else -> Unit
                    }
                }.launchIn(this)

            viewModel.callState.onEach {
                if(it is Call.State.Disconnected) finish()
                // TODO aggiungere messaggio in caso di errore?
            }.launchIn(this)

            viewModel.participants.collect { participants ->
                participants.others.plus(participants.me).forEach { participant ->
                    participant.streams.onEach { streams ->
                        streams.forEach { stream ->
                            val index = itemAdapter!!.adapterItems.indexOfFirst { item -> item.data.stream.id == stream.id }
                            if (index == -1) itemAdapter!!.add(StreamItem(StreamItemData(participant == participants.me, participant.username, participant.avatarUrl, stream), this))
                            else itemAdapter!![index] = StreamItem(StreamItemData(participant == participants.me, participant.username, participant.avatarUrl, stream), this)
                        }
                    }.launchIn(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryObserver.stop()
        wifiObserver.stop()
        _binding = null
        decorView = null
        itemAdapter!!.clear()
        itemAdapter = null
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
            if (it) notificationManager.dismiss(false)
            notificationManager.dnd = it
        }

        // Update state bar
        with(binding.bandyerStatusBar) {
            hideCenteredTitle()
            setBackgroundColor(Color.TRANSPARENT)
            show()

            when (destinationId) {
                R.id.ringingFragment, R.id.dialingFragment, R.id.connectingFragment, R.id.endCallFragment, R.id.callEndedFragment -> hide()
                R.id.callEndedFragment, R.id.chatFragment, R.id.chatMenuFragment -> applyFlatTint()
                R.id.participantsFragment -> {
                    applyFlatTint(); showCenteredTitle()
                }
            }
        }
    }

    // GESTURES AND KEYS EVENTS
    /**
     * @suppress
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Dispatch the touch event to the gesture detector
        return if (ev != null && glassGestureDetector.onTouchEvent(ev)) true
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
            TouchEvent.Type.TAP -> true.also { notificationManager.expand() }
            TouchEvent.Type.SWIPE_DOWN -> true.also { notificationManager.dismiss() }
            else -> false
        }

    // NOTIFICATION LISTENER
    override fun onShow() {
        isNotificationVisible = true
    }

    override fun onExpanded() {
        isNotificationVisible = false
        if (supportFragmentManager.currentNavigationFragment?.id != R.id.smartglass_nav_graph_chat)
            navController.navigate(R.id.smartglass_nav_graph_chat)
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
        notificationManager.addListener(listener)
    }

    /**
     * Remove a notification listener
     *
     * @param listener NotificationListener
     */
    fun removeNotificationListener(listener: ChatNotificationManager.NotificationListener) {
        notificationManager.removeListener(listener)
    }

    // IMMERSIVE MODE
    private fun enterImmersiveMode() {
        supportActionBar?.hide()
        decorView = window.decorView
        decorView!!.apply {
            setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                    hideSystemUI()
            }
        }
    }

    private fun hideSystemUI() {
        decorView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // UPDATE STATUS BAR UI
    private fun StatusBarView.applyFlatTint() = setBackgroundColor(
        ResourcesCompat.getColor(
            resources,
            R.color.bandyer_glass_dimmed_background_color,
            null
        )
    )

    private fun StatusBarView.updateCenteredText(nCallParticipants: Int) =
        setCenteredText(
            resources.getString(
                R.string.bandyer_glass_users_in_call_pattern,
                nCallParticipants
            )
        )

    private fun StatusBarView.updateBatteryIcon(batteryInfo: BatteryInfo) {
        setBatteryChargingState(batteryInfo.state == BatteryInfo.State.CHARGING)
        setBatteryCharge(batteryInfo.percentage)
    }

    private fun StatusBarView.updateWifiSignalIcon(wifiInfo: WiFiInfo) {
        setWiFiSignalState(
            if (wifiInfo.state == WiFiInfo.State.DISABLED) StatusBarView.WiFiSignalState.DISABLED
            else when (wifiInfo.level) {
                WiFiInfo.Level.NO_SIGNAL, WiFiInfo.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                WiFiInfo.Level.FAIR, WiFiInfo.Level.GOOD -> StatusBarView.WiFiSignalState.MODERATE
                WiFiInfo.Level.EXCELLENT -> StatusBarView.WiFiSignalState.FULL
            }
        )
    }
}

internal fun NavController.safeNavigate(direction: NavDirections): Boolean =
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction); true } ?: false

fun <T : GenericItem> ItemAdapter<T>.addOrUpdate(items: List<T>) =
    FastAdapterDiffUtil.calculateDiff(this, items, true).also { FastAdapterDiffUtil[this] = it }