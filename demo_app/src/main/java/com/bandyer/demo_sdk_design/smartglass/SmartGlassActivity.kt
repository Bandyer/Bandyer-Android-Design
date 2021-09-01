package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.demo_sdk_design.databinding.ActivitySmartGlassBinding
import com.bandyer.demo_sdk_design.smartglass.battery.BatteryObserver
import com.bandyer.demo_sdk_design.smartglass.battery.BatteryState
import com.bandyer.demo_sdk_design.smartglass.network.CellSignalObserver
import com.bandyer.demo_sdk_design.smartglass.network.WiFiObserver
import com.bandyer.demo_sdk_design.smartglass.network.WiFiState
import com.bandyer.sdk_design.new_smartglass.GlassGestureDetector
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEventListener
import com.bandyer.sdk_design.new_smartglass.chat.notification.NotificationData
import com.bandyer.sdk_design.new_smartglass.chat.notification.NotificationManager
import com.bandyer.sdk_design.new_smartglass.status_bar.StatusBarView
import com.bandyer.sdk_design.new_smartglass.utils.currentNavigationFragment
import kotlinx.coroutines.flow.collect

class SmartGlassActivity : AppCompatActivity(), GlassGestureDetector.OnGestureListener,
    NotificationManager.NotificationListener, SmartGlassTouchEventListener {

    private lateinit var binding: ActivitySmartGlassBinding

    private var statusBar: StatusBarView? = null

    private val currentFragment: Fragment?
        get() = supportFragmentManager.currentNavigationFragment
    private lateinit var decorView: View

    private lateinit var glassGestureDetector: GlassGestureDetector
    private lateinit var notificationManager: NotificationManager
    private var handleNotification = false
    private lateinit var batteryObserver: BatteryObserver
    private lateinit var wifiObserver: WiFiObserver
//    private val internetObserver = InternetObserver(5000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartGlassBinding.inflate(layoutInflater)
        statusBar = binding.statusBar
        setContentView(binding.root)

        enterImmersiveMode()

        glassGestureDetector = GlassGestureDetector(this, this)
        notificationManager = NotificationManager(binding.content, this)
        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)

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
        statusBar!!.setCenteredText(resources.getString(R.string.bandyer_smartglass_users_in_call, 3))

//        Handler(Looper.getMainLooper()).postDelayed({
//            notificationManager.show(
//                    listOf(NotificationData(
//                        "Mario",
//                        "Mario",
//                        "Il numero seriale del macchinario dovrebbe essere AR56000TY7-1824\\nConfermi?",
//                        R.drawable.sample_image
//                    ))
//            )
//        }, 4000)

//        Handler(Looper.getMainLooper()).postDelayed({
//            notificationManager.show(
//                listOf(
//                    NotificationData(
//                        "Mario",
//                        "Mario",
//                        "Il numero seriale del macchinario dovrebbe essere AR56000TY7-1824\\nConfermi?",
//                        R.drawable.sample_image
//                    ),
//                    NotificationData(
//                        "Gianfranco",
//                        "Gianfranco",
//                        "Mi piacciono i treni",
//                        null
//                    ),
//                    NotificationData(
//                        "Mario",
//                        "Mario",
//                        "Ciao",
//                        R.drawable.sample_image
//                    )
//                )
//            )
//        }, 5000)
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
            handleSmartGlassTouchEvent(SmartGlassTouchEvent.getEvent(event))
        ) true
        else super.dispatchKeyEvent(event)
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean =
        handleSmartGlassTouchEvent(SmartGlassTouchEvent.getEvent(gesture))

    private fun handleSmartGlassTouchEvent(smartGlassEvent: SmartGlassTouchEvent): Boolean =
        if (handleNotification) onSmartGlassTouchEvent(smartGlassEvent)
        else (currentFragment as? SmartGlassTouchEventListener)?.onSmartGlassTouchEvent(
            smartGlassEvent
        ) ?: false

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean =
        when (event.type) {
            SmartGlassTouchEvent.Type.TAP -> {
                notificationManager.expand()
                true
            }
            SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
                notificationManager.dismiss()
                true
            }
            else -> false
        }

    // NOTIFICATION LISTENER

    override fun onShow() {
        handleNotification = true
    }

    override fun onExpanded() {
        handleNotification = false
        // TODO Fragment NavHostFragment{199be23} (47d28a1a-fa6e-41d5-81e7-090f54d21e92) has not been attached yet.
        if (supportFragmentManager.currentNavigationFragment?.id != R.id.chatFragment)
            binding.navHostFragment.findNavController().navigate(R.id.chatFragment)
    }

    override fun onDismiss() {
        handleNotification = false
    }

    fun hideNotification() {
        handleNotification = false
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