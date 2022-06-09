package com.kaleyra.collaboration_suite_glass_ui.chat.notification

import android.animation.Animator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.notification.ChatNotificationMessage
import com.kaleyra.collaboration_suite_core_ui.notification.CustomChatNotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOff
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.StringExtensions.parseToColor
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.animateViewHeight
import com.kaleyra.collaboration_suite_glass_ui.GlassTouchEventManager
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.TouchEvent
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.common.AvatarGroupView
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatNotificationActivityGlassBinding
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue


class GlassChatNotificationActivity : AppCompatActivity(), GlassTouchEventManager.Listener {

    private lateinit var binding: KaleyraChatNotificationActivityGlassBinding

    private var glassTouchEventManager: GlassTouchEventManager? = null
    private var sendersId = ConcurrentLinkedQueue<String>()
    private var msgsPerChat = ConcurrentHashMap<String, Int>()
    private var isLayoutExpanded = false
    private var handler: Handler? = null

    /**
     * @suppress
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOn()
        binding =
            KaleyraChatNotificationActivityGlassBinding.inflate(layoutInflater)
                .apply {
                    setListenersForRealWear(kaleyraBottomNavigation)
                }

        val message = getMessage(intent)
        val nOfMessages = getNOfMessages(intent)
        msgsPerChat[getChatId(intent)] = nOfMessages
        if (nOfMessages == 1) setSingleMessageUI(message) else setMultipleMessagesUI(message, nOfMessages)
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
        msgsPerChat[getChatId(intent)] = getNOfMessages(intent)
        Log.e("nOfMessages-not", "${getNOfMessages(intent)}" )
        setMultipleMessagesUI(getMessage(intent), msgsPerChat.values.sum())
    }

    private fun setSingleMessageUI(message: ChatNotificationMessage) = with(binding) {
        kaleyraTitle.text = message.username
        kaleyraMessage.text = message.text
        kaleyraMessage.maxLines = 2
        kaleyraTime.visibility = View.VISIBLE
        sendersId.add(message.userId)
        kaleyraAvatars.clean()
        kaleyraAvatars.addAvatar(message)
    }

    private fun setMultipleMessagesUI(message: ChatNotificationMessage, counter: Int) = with(binding) {
        kaleyraMessage.text = null
        kaleyraMessage.maxLines = 0
        kaleyraTime.visibility = View.GONE
        kaleyraTitle.text = resources.getString(R.string.kaleyra_glass_new_messages_pattern, counter)
        if (sendersId.contains(message.userId)) return@with
        sendersId.add(message.userId)
        kaleyraAvatars.addAvatar(message)
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
        turnScreenOff()
        glassTouchEventManager = null
        sendersId.clear()
        msgsPerChat.clear()
        handler = null
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

    private fun getMessage(intent: Intent) =
        ChatNotificationMessage(
            intent.getStringExtra("userId") ?: "null",
            intent.getStringExtra("username") ?: "null",
            intent.getParcelableExtra("avatar") as? Uri ?: Uri.EMPTY,
            intent.getStringExtra("message") ?: "null",
             intent.getLongExtra("timestamp", 0L)
        )

    private fun getChatId(intent: Intent) = intent.extras?.getString("chatId") ?: ""

    private fun getNOfMessages(intent: Intent) = intent.extras?.getInt("nOfMessages") ?: 0

    private fun AvatarGroupView.addAvatar(data: ChatNotificationMessage) {
        if (data.avatar == Uri.EMPTY)
            addAvatar(
                data.username[0].uppercase(),
                data.userId.parseToColor()
            )
        else addAvatar(data.avatar)
    }

    private fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        bottomNavView.setThirdItemListener { onSwipeDown() }
        bottomNavView.setSecondItemListener { onTap() }
    }

    private fun onTap() {
        expandRootView {
            isLayoutExpanded = true
            binding.kaleyraMessage.maxLines = Int.MAX_VALUE
            val chat = CollaborationUI.chatBox.chats.value.first { it.id == getChatId(intent) }
            CollaborationUI.chatBox.show(this@GlassChatNotificationActivity, chat)
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