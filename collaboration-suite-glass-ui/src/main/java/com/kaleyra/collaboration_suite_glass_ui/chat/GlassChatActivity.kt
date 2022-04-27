package com.kaleyra.collaboration_suite_glass_ui.chat

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.kaleyra.collaboration_suite_core_ui.CollaborationService
import com.kaleyra.collaboration_suite_core_ui.chat.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.chat.ChatUIDelegate
import com.kaleyra.collaboration_suite_core_ui.common.DeviceStatusDelegate
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.TouchEvent
import com.kaleyra.collaboration_suite_glass_ui.TouchEventListener
import com.kaleyra.collaboration_suite_glass_ui.common.OnDestinationChangedListener
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatActivityGlassBinding
import com.kaleyra.collaboration_suite_glass_ui.status_bar_views.StatusBarView
import com.kaleyra.collaboration_suite_glass_ui.utils.GlassGestureDetector
import com.kaleyra.collaboration_suite_glass_ui.utils.currentNavigationFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_utils.battery_observer.BatteryInfo
import com.kaleyra.collaboration_suite_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class GlassChatActivity : ChatActivity(), GlassGestureDetector.OnGestureListener,
    TouchEventListener, OnDestinationChangedListener {

    private lateinit var binding: KaleyraChatActivityGlassBinding

    private var service: CollaborationService? = null

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            service as ChatUIDelegate,
            service as DeviceStatusDelegate
        )
    }

    private var glassGestureDetector: GlassGestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.kaleyra_chat_activity_glass)
        glassGestureDetector = GlassGestureDetector(this, this)
//        enableImmersiveMode()
    }

    override fun onDestroy() {
        super.onDestroy()
        service = null
        glassGestureDetector = null
    }

    override fun onServiceBound(service: CollaborationService) {
        this.service = service

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
        }
    }

    override fun onDestinationChanged(destinationId: Int) = Unit

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Dispatch the touch event to the gesture detector
        return if (ev != null && glassGestureDetector!!.onTouchEvent(ev)) true
        else super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean =
        if (event?.action == MotionEvent.ACTION_DOWN &&
            handleSmartGlassTouchEvent(TouchEvent.getEvent(event))
        ) true
        else super.dispatchKeyEvent(event)

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean =
        handleSmartGlassTouchEvent(TouchEvent.getEvent(gesture))

    private fun handleSmartGlassTouchEvent(glassEvent: TouchEvent): Boolean {
        val currentDest =
            supportFragmentManager.currentNavigationFragment as? TouchEventListener
                ?: return false
        return if (!currentDest.onTouch(glassEvent)) onTouch(glassEvent) else true
    }

    override fun onTouch(event: TouchEvent): Boolean = false
}