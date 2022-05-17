package com.kaleyra.collaboration_suite_glass_ui.chat

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.animateViewHeight
import com.kaleyra.collaboration_suite_glass_ui.GlassTouchEventManager
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.TouchEvent
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatNotificationActivityGlassBinding

object NotificationLauncher {
    fun show(context: Context) {
        context.startActivity(Intent(context, ChatNotificationActivity::class.java))
    }
}

internal class ChatNotificationActivity : AppCompatActivity(), GlassTouchEventManager.Listener {

    private lateinit var binding: KaleyraChatNotificationActivityGlassBinding

    private var glassTouchEventManager: GlassTouchEventManager? = null
    private var isLayoutExpanded = false

    /**
     * @suppress
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView<KaleyraChatNotificationActivityGlassBinding?>(
                this,
                R.layout.kaleyra_chat_notification_activity_glass
            ).apply {
                setListenersForRealWear(kaleyraBottomNavigation)
            }

        glassTouchEventManager = GlassTouchEventManager(this, this)

        overridePendingTransition(R.anim.kaleyra_notification_slide_down, R.anim.kaleyra_nothing)
    }

    /**
     * @suppress
     */
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isLayoutExpanded) return@postDelayed
            finish()
        }, AUTO_FINISH_TIME)
        return super.onCreateView(name, context, attrs)
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        glassTouchEventManager = null
    }

    /**
     * @suppress
     */
    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.kaleyra_nothing,
            if (isLayoutExpanded) R.anim.kaleyra_nothing else R.anim.kaleyra_notification_slide_up
        )
    }

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

    /**
     * @suppress
     */
    override fun onGlassTouchEvent(glassEvent: TouchEvent): Boolean =
        when (glassEvent.type) {
            TouchEvent.Type.TAP -> {
                onTap(); true
            }
            TouchEvent.Type.SWIPE_DOWN -> {
                onSwipeDown(); true
            }
            else -> false
        }

    private fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        bottomNavView.setThirdItemListener { onSwipeDown() }
        bottomNavView.setSecondItemListener { onTap() }
    }

    private fun onTap() {
        isLayoutExpanded = true
        expandRootView()
    }

    private fun onSwipeDown() {
        finish()
    }

    private fun expandRootView(onExpanded: ((Animator) -> Unit)? = null) {
        val rootView = binding.root
        rootView.animateViewHeight(
            rootView.height,
            (rootView.parent as View).height,
            ANIMATION_DURATION,
            AccelerateDecelerateInterpolator()
        ) { onExpanded?.invoke(it) }
    }

    private companion object {
        const val AUTO_FINISH_TIME = 3000L
        const val ANIMATION_DURATION = 500L
    }
}