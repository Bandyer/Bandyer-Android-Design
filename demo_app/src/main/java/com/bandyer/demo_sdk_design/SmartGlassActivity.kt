package com.bandyer.demo_sdk_design

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bandyer.demo_sdk_design.databinding.ActivitySmartGlassBinding
import com.bandyer.sdk_design.new_smartglass.GlassGestureDetector
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEventListener
import com.bandyer.sdk_design.new_smartglass.chat.notification.NotificationManager
import com.bandyer.sdk_design.new_smartglass.utils.battery.BatteryObserver
import com.bandyer.sdk_design.new_smartglass.utils.currentNavigationFragment
import com.bandyer.sdk_design.new_smartglass.utils.network.WiFiObserver

class SmartGlassActivity : AppCompatActivity(), GlassGestureDetector.OnGestureListener,
    NotificationManager.NotificationListener, SmartGlassTouchEventListener {

    private lateinit var binding: ActivitySmartGlassBinding

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
        setContentView(binding.root)

        enterImmersiveMode()

        glassGestureDetector = GlassGestureDetector(this, this)
        notificationManager = NotificationManager(binding.content, this)
        batteryObserver = BatteryObserver(this)
        wifiObserver = WiFiObserver(this)

//        Handler(Looper.getMainLooper()).postDelayed({
//            notificationManager.show("Mario: Il numero seriale del macchinario dovrebbe essere AR56000TY7-1824\\nConfermi?")
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?) =
        handleSmartGlassTouchEvent(SmartGlassTouchEvent.getEvent(keyCode, event)) ?: false

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean =
        handleSmartGlassTouchEvent(SmartGlassTouchEvent.getEvent(gesture)) ?: false

    private fun handleSmartGlassTouchEvent(smartGlassEvent: SmartGlassTouchEvent.Event): Boolean? =
        if (handleNotification) onSmartGlassTouchEvent(smartGlassEvent)
        else (currentFragment as? SmartGlassTouchEventListener)?.onSmartGlassTouchEvent(
            smartGlassEvent
        )

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean =
        when (event) {
            SmartGlassTouchEvent.Event.TAP -> {
                notificationManager.expand()
                true
            }
            SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
                notificationManager.dismiss()
                true
            }
            else -> false
        }

    // NOTIFICATION LISTENER

    override fun onShow() {
        handleNotification = true
//        (supportFragmentManager.currentNavigationFragment as? BottomBarHolder)?.hideBottomBar()
        showNotificationBottomBar()
    }

    override fun onExpanded() {
        handleNotification = false
        if(supportFragmentManager.currentNavigationFragment?.id != R.id.chatFragment)
            binding.navHostFragment.findNavController().navigate(R.id.chatFragment)
//        (supportFragmentManager.currentNavigationFragment as? BottomBarHolder)?.showBottomBar()
        hideNotificationBottomBar()
        hideNotification()
    }

    override fun onDismiss() {
        handleNotification = false
//        (supportFragmentManager.currentNavigationFragment as? BottomBarHolder)?.showBottomBar()
        hideNotificationBottomBar()
    }

    fun hideNotification() {
        handleNotification = false
        notificationManager.dismiss(false)
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

    private fun hideNotificationBottomBar() {
        binding.notificationBottomActionBar.visibility = View.GONE
    }

    private fun showNotificationBottomBar() {
        binding.notificationBottomActionBar.visibility = View.VISIBLE
    }
}