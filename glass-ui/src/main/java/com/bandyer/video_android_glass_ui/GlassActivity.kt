package com.bandyer.video_android_glass_ui

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.databinding.BandyerActivityGlassBinding
import com.bandyer.video_android_glass_ui.status_bar_views.StatusBarView
import com.bandyer.video_android_glass_ui.utils.GlassGestureDetector
import com.bandyer.video_android_glass_ui.utils.currentNavigationFragment
import com.bandyer.video_android_glass_ui.utils.observers.battery.BatteryObserver
import com.bandyer.video_android_glass_ui.utils.observers.battery.BatteryState
import com.bandyer.video_android_glass_ui.utils.observers.network.WiFiObserver
import com.bandyer.video_android_glass_ui.utils.observers.network.WiFiState
import kotlinx.coroutines.flow.collect

class GlassActivity : AppCompatActivity(), GlassGestureDetector.OnGestureListener, ChatNotificationManager.NotificationListener, TouchEventListener {

    private lateinit var binding: BandyerActivityGlassBinding
    private lateinit var decorView: View

    private val currentFragment: Fragment?
        get() = supportFragmentManager.currentNavigationFragment
    private lateinit var navController: NavController

    // Google Glass gesture detector
    private lateinit var glassGestureDetector: GlassGestureDetector

    // Notification
    private lateinit var notificationManager: ChatNotificationManager
    private var isNotificationVisible = false

    // Observers status bar UI
    private lateinit var batteryObserver: BatteryObserver
    private lateinit var wifiObserver: WiFiObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BandyerActivityGlassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enterImmersiveMode()

        // NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Gesture Detector
        glassGestureDetector = GlassGestureDetector(this, this)

        // Notification Manager
        notificationManager = ChatNotificationManager(binding.content).also { it.addListener(this) }

        // Battery observer
        batteryObserver = BatteryObserver(this)
        lifecycleScope.launchWhenStarted {
            batteryObserver.observe().collect { binding.statusBar.updateBatteryIcon(it) }
        }

        // WiFi observer
        wifiObserver = WiFiObserver(this)
        lifecycleScope.launchWhenStarted {
            wifiObserver.observe().collect { binding.statusBar.updateWifiSignalIcon(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onStop() {
        super.onStop()
        batteryObserver.stop()
        wifiObserver.stop()
    }

    /**
     *  Handle the status bar UI and the notification when the destination fragment
     *  on the nav graph is changed.
     *
     *  NavController.OnDestinationChangedListener is not used because the code
     *  need to be executed when the fragment is actually being created.
     *
     *  @param destinationId The destination fragment's id
     */
    fun onDestinationChanged(destinationId: Int) {
        (destinationId == R.id.chatFragment).also {
            if(it) notificationManager.dismiss(false)
            notificationManager.dnd = it
        }

        // Update status bar
        with(binding.statusBar) {
            hideCenteredTitle()
            setBackgroundColor(Color.TRANSPARENT)
            show()

            when (destinationId) {
                R.id.ringingFragment, R.id.endCallFragment, R.id.callEndedFragment -> hide()
                R.id.callEndedFragment, R.id.chatFragment, R.id.chatMenuFragment -> applyFlatTint()
                R.id.participantsFragment -> { applyFlatTint(); showCenteredTitle() }
            }
        }
    }

    // GESTURES AND KEYS EVENTS
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Dispatch the touch event to the gesture detector
        return if (ev != null && glassGestureDetector.onTouchEvent(ev)) true
        else super.dispatchTouchEvent(ev)
    }

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
    override fun onShow() { isNotificationVisible = true }

    override fun onExpanded() {
        isNotificationVisible = false
        if (supportFragmentManager.currentNavigationFragment?.id != R.id.smartglass_nav_graph_chat)
            navController.navigate(R.id.smartglass_nav_graph_chat)
    }

    override fun onDismiss() { isNotificationVisible = false }

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
        decorView.apply {
            setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                    hideSystemUI()
            }
        }
    }

    private fun hideSystemUI() {
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // UPDATE STATUS BAR UI
    private fun StatusBarView.applyFlatTint() = setBackgroundColor(ResourcesCompat.getColor(resources, R.color.bandyer_glass_background_color, null))

    private fun StatusBarView.updateCenteredText(nCallParticipants: Int) =
        binding.statusBar.setCenteredText(resources.getString(R.string.bandyer_glass_users_in_call_pattern, nCallParticipants))

    private fun StatusBarView.updateBatteryIcon(batteryState: BatteryState) {
        setBatteryChargingState(batteryState.status == BatteryState.Status.CHARGING)
        setBatteryCharge(batteryState.percentage)
    }

    private fun StatusBarView.updateWifiSignalIcon(wifiState: WiFiState) {
        setWiFiSignalState(
            if (wifiState.state == WiFiState.State.DISABLED) StatusBarView.WiFiSignalState.DISABLED
            else when (wifiState.level) {
                WiFiState.Level.NO_SIGNAL, WiFiState.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                WiFiState.Level.FAIR, WiFiState.Level.GOOD -> StatusBarView.WiFiSignalState.MODERATE
                WiFiState.Level.EXCELLENT -> StatusBarView.WiFiSignalState.FULL
            }
        )
    }
}