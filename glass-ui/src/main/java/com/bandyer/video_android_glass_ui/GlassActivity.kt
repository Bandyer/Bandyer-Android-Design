package com.bandyer.video_android_glass_ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.bandyer.video_android_glass_ui.call.DialingFragment
import com.bandyer.video_android_glass_ui.call.DialingFragmentDirections
import com.bandyer.video_android_glass_ui.call.RingingFragmentDirections
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.databinding.BandyerActivityGlassBinding
import com.bandyer.video_android_glass_ui.status_bar_views.StatusBarView
import com.bandyer.video_android_glass_ui.utils.GlassGestureDetector
import com.bandyer.video_android_glass_ui.utils.currentNavigationFragment
import com.bandyer.video_android_glass_ui.utils.observers.battery.BatteryInfo
import com.bandyer.video_android_glass_ui.utils.observers.battery.BatteryObserver
import com.bandyer.video_android_glass_ui.utils.observers.network.WiFiInfo
import com.bandyer.video_android_glass_ui.utils.observers.network.WiFiObserver
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    @Suppress("UNCHECKED_CAST")
    private val viewModel: GlassViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                GlassViewModel(ProvidersHolder.callProvider!!) as T
        }
    }

    // ADAPTER
    private var itemAdapter: ItemAdapter<CallParticipantItem>? = null

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
        _binding = DataBindingUtil.setContentView(this, R.layout.bandyer_activity_glass)

        enterImmersiveMode()

        with(binding.bandyerStreams) {
            itemAdapter = ItemAdapter()
            val fastAdapter = FastAdapter.with(itemAdapter!!)
            val layoutManager =
                LinearLayoutManager(this@GlassActivity, LinearLayoutManager.HORIZONTAL, false)
            val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

            this.layoutManager = layoutManager
            adapter = fastAdapter
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
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    batteryObserver.observe()
                        .collect { binding.bandyerStatusBar.updateBatteryIcon(it) }
                }

                launch {
                    wifiObserver.observe()
                        .collect { binding.bandyerStatusBar.updateWifiSignalIcon(it) }
                }

                launch {
                    viewModel.call.collect { call ->
                        call.state.combine(call.participants) { state, participants ->
                            when {
                                state is Call.State.Connecting && participants.me == participants.creator -> navController.navigate(R.id.dialingFragment)
                                state is Call.State.Connecting -> navController.navigate(R.id.ringingFragment)
                                state is Call.State.Reconnecting -> Unit
                                state is Call.State.Connected -> {
                                    navController.safeNavigate(
                                        if(currentFragment is DialingFragment) DialingFragmentDirections.actionDialingFragmentToEmptyFragment()
                                        else RingingFragmentDirections.actionRingingFragmentToEmptyFragment()
                                    )
                                }
                                state is Call.State.Disconnected.Ended -> Unit
                                state is Call.State.Disconnected.Error -> Unit
                            }
                        }.collect()
                    }
                }

                launch {
                    viewModel.participants.collect {  participants ->
                        participants.others.plus(participants.me).onEach { part ->
                            part.streams.onEach { streams ->
                                streams.onEach { stream ->
                                    stream.video.onEach { video ->
                                        video?.view?.onEach { view ->
                                            itemAdapter!!.add(CallParticipantItem(stream.id, view, lifecycle))
                                        }?.launchIn(this)
                                    }.launchIn(this)
                                }
                            }.launchIn(this)
                        }
                    }
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
        return if (event?.action == MotionEvent.ACTION_DOWN && handleSmartGlassTouchEvent(TouchEvent.getEvent(event))) true
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
            R.color.bandyer_glass_background_color,
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

internal fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
}

fun <T: GenericItem> ItemAdapter<T>.addOrUpdate(items: List<T>) =
    FastAdapterDiffUtil.calculateDiff(this, items, true).also { FastAdapterDiffUtil[this] = it }