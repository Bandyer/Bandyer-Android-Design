package com.kaleyra.collaboration_suite_glass_ui.chat.notification

import android.animation.Animator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.notification.CustomChatNotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.StringExtensions.parseToColor
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.animateViewHeight
import com.kaleyra.collaboration_suite_glass_ui.GlassTouchEventManager
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.TouchEvent
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.common.AvatarGroupView
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatNotificationActivityGlassBinding
import java.util.concurrent.ConcurrentLinkedQueue


class GlassChatNotificationActivity : AppCompatActivity(), GlassTouchEventManager.Listener {

    private data class NotificationData(
        val username: String,
        val userId: String,
        val message: String,
        val imageUri: Uri
    )

    private lateinit var binding: KaleyraChatNotificationActivityGlassBinding

    private var glassTouchEventManager: GlassTouchEventManager? = null
    private var participants: Array<String>? = null
    private var sendersId = ConcurrentLinkedQueue<String>()
    private var isLayoutExpanded = false
    private var msgCounter = 1
    private var handler: Handler? = null

    /**
     * @suppress
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            KaleyraChatNotificationActivityGlassBinding.inflate(layoutInflater)
                .apply {
                    setListenersForRealWear(kaleyraBottomNavigation)
                    val data = getNotificationData(intent)
                    kaleyraTitle.text = data.username
                    kaleyraMessage.text = data.message
                    kaleyraMessage.maxLines = 2
                    kaleyraTime.visibility = View.VISIBLE
                    kaleyraAvatars.addAvatar(data)
                    participants = intent.getStringArrayExtra("participants")
                    sendersId.add(data.userId)
                }

        setContentView(binding.root)
        glassTouchEventManager = GlassTouchEventManager(this, this)
    }


    override fun onResume() {
        super.onResume()
        setDismiss()
    }


    /**
     * @suppress
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        setDismiss()
        val data = getNotificationData(intent)
        binding.kaleyraMessage.text = null
        binding.kaleyraMessage.maxLines = 0
        binding.kaleyraTime.visibility = View.GONE
        binding.kaleyraTitle.text =
            resources.getString(R.string.kaleyra_glass_new_messages_pattern, ++msgCounter)

        if (sendersId.contains(data.userId)) return
        sendersId.add(data.userId)
        if (sendersId.count() > 1) binding.kaleyraAvatars.addAvatar(data)
    }

    private fun setDismiss() {
        handler?.removeCallbacksAndMessages(null)
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed({
                                 if (isLayoutExpanded) return@postDelayed
                                 handler = null
                                 binding.root.startAnimation(AnimationUtils.loadAnimation(this, R.anim.kaleyra_notification_slide_up))
                                 binding.root.postOnAnimation {
                                     binding.root.visibility = View.INVISIBLE
                                     overridePendingTransition(0, 0)
                                 }
                                 finishAndRemoveTask()
                             }, CustomChatNotificationManager.AUTO_DISMISS_TIME)
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

    private fun getNotificationData(intent: Intent) =
        NotificationData(
            intent.getStringExtra("username") ?: "null",
            intent.getStringExtra("userId") ?: "null",
            intent.getStringExtra("message") ?: "null",
            intent.getParcelableExtra("imageUri") as? Uri ?: Uri.EMPTY
        )

    private fun AvatarGroupView.addAvatar(data: NotificationData) {
        if (data.imageUri == Uri.EMPTY)
            addAvatar(
                data.username[0].uppercase(),
                data.userId.parseToColor()
            )
        else addAvatar(data.imageUri)
    }

    private fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        bottomNavView.setThirdItemListener { onSwipeDown() }
        bottomNavView.setSecondItemListener { onTap() }
    }

    private fun onTap() {
        expandRootView {
            isLayoutExpanded = true
            binding.kaleyraMessage.maxLines = Int.MAX_VALUE
            participants?.also { parts ->
                CollaborationUI.chatBox.show(CollaborationUI.chatBox.create(
                    parts.map {
                        object : User {
                            override val userId = it
                        }
                    }
                ))
            }

            finishAndRemoveTask()
        }
    }

    private fun onSwipeDown() {
        finishAndRemoveTask()
    }

    private fun expandRootView(onExpanded: ((Animator) -> Unit)? = null) {
        val rootView = binding.kaleyraRoot
        rootView.animateViewHeight(
            rootView.height,
            (rootView.parent as View).height,
            ANIMATION_DURATION,
            AccelerateDecelerateInterpolator()
        ) { onExpanded?.invoke(it) }
    }

    private companion object {
        const val ANIMATION_DURATION = 300L
    }
}