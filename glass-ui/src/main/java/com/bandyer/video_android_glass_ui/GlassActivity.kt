package com.bandyer.video_android_glass_ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationData
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

class GlassActivity : AppCompatActivity(), GlassGestureDetector.OnGestureListener,
                           ChatNotificationManager.NotificationListener, TouchEventListener {

    private lateinit var binding: BandyerActivityGlassBinding

    private var statusBar: StatusBarView? = null

    private val currentFragment: Fragment?
        get() = supportFragmentManager.currentNavigationFragment
    private lateinit var decorView: View

    private lateinit var glassGestureDetector: GlassGestureDetector
    private lateinit var notificationManager: ChatNotificationManager
    private var isNotificationVisible = false
    private lateinit var batteryObserver: BatteryObserver
    private lateinit var wifiObserver: WiFiObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BandyerActivityGlassBinding.inflate(layoutInflater)
        statusBar = binding.statusBar
        setContentView(binding.root)

        enterImmersiveMode()

        glassGestureDetector = GlassGestureDetector(this, this)
        notificationManager = ChatNotificationManager(binding.content)
        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)

        notificationManager.addListener(this)

        lifecycleScope.launchWhenStarted {
            batteryObserver.observe().collect {
                statusBar!!.setBatteryChargingState(it.status == BatteryState.Status.CHARGING)
                statusBar!!.setBatteryCharge(it.percentage)
            }
        }

        lifecycleScope.launchWhenStarted {
            wifiObserver.observe().collect {
                statusBar!!.setWiFiSignalState(
                    if (it.state == WiFiState.State.DISABLED)
                        StatusBarView.WiFiSignalState.DISABLED
                    else
                        when (it.level) {
                            WiFiState.Level.NO_SIGNAL -> StatusBarView.WiFiSignalState.LOW
                            WiFiState.Level.POOR -> StatusBarView.WiFiSignalState.LOW
                            WiFiState.Level.FAIR -> StatusBarView.WiFiSignalState.MODERATE
                            WiFiState.Level.GOOD -> StatusBarView.WiFiSignalState.MODERATE
                            WiFiState.Level.EXCELLENT -> StatusBarView.WiFiSignalState.FULL
                        }
                )
            }
        }

        statusBar!!.hideCenteredTitle()
        statusBar!!.setCenteredText(
            resources.getString(
                R.string.bandyer_glass_users_in_call_pattern,
                3
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager.show(
                listOf(
                    ChatNotificationData(
                        "Mario",
                        "Mario",
                        "Il numero seriale del macchinario dovrebbe essere AR56000TY7-1824\\nConfermi?",
                        null
                    )
                )
            )
        }, 4000)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryObserver.stop()
        wifiObserver.stop()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Dispatch the touch event to the gesture detector
        return if (ev != null && glassGestureDetector.onTouchEvent(ev)) true
        else super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (
            event?.action == MotionEvent.ACTION_DOWN &&
            handleSmartGlassTouchEvent(TouchEvent.getEvent(event))
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
        // TODO Fragment NavHostFragment{199be23} (47d28a1a-fa6e-41d5-81e7-090f54d21e92) has not been attached yet.
        if (supportFragmentManager.currentNavigationFragment?.id != R.id.smartglass_nav_graph_chat)
            binding.navHostFragment.findNavController().navigate(R.id.smartglass_nav_graph_chat)
    }

    override fun onDismiss() {
        isNotificationVisible = false
    }

    fun addNotificationListener(listener: ChatNotificationManager.NotificationListener) {
        notificationManager.addListener(listener)
    }

    fun removeNotificationListener(listener: ChatNotificationManager.NotificationListener) {
        notificationManager.removeListener(listener)
    }

    fun hideNotification() {
        isNotificationVisible = false
        notificationManager.dismiss(false)
    }

    fun hideStatusBar() {
        binding.statusBar.visibility = View.GONE
    }

    fun showStatusBar() {
        binding.statusBar.visibility = View.VISIBLE
    }

    fun hideStatusBarCenteredTitle() {
        binding.statusBar.hideCenteredTitle()
    }

    fun showStatusBarCenteredTitle() {
        binding.statusBar.showCenteredTitle()
    }

    fun setStatusBarColor(@ColorInt color: Int?) {
        binding.statusBar.setBackgroundColor(color)
    }

    fun setDnd(value: Boolean) {
        notificationManager.dnd = value
    }

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
}