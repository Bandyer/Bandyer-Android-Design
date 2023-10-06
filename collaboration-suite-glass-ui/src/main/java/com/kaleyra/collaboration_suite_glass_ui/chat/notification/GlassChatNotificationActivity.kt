/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraChatNotificationActivityGlassBinding
import com.kaleyra.collaboration_suite.conversation.Chat
import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite.conversation.OtherMessage
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.KaleyraVideo
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOff
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.StringExtensions.parseToColor
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.animateViewHeight
import com.kaleyra.collaboration_suite_glass_ui.GlassTouchEventManager
import com.kaleyra.collaboration_suite_glass_ui.TouchEvent
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.common.AvatarGroupView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * The glass chat custom notification activity
 */
internal class GlassChatNotificationActivity : AppCompatActivity(), GlassTouchEventManager.Listener {

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
        binding = KaleyraChatNotificationActivityGlassBinding.inflate(layoutInflater).apply {
            setListenersForRealWear(kaleyraBottomNavigation)
        }
        updateUI(intent)
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
        updateUI(intent)
    }

    private fun updateUI(intent: Intent) = MainScope().launch {
        val chat = getChat(intent) ?: return@launch
        val nOfMessages = getNOfUnreadMessages(chat)
        msgsPerChat[chat.id] = nOfMessages
        val message = getLastReceivedMessage(chat) ?: return@launch
        val participant = message.creator
        val username = participant.combinedDisplayName.first() ?: ""
        val avatar = participant.combinedDisplayImage.first() ?: Uri.EMPTY
        val text = (message.content as? Message.Content.Text)?.message ?: ""
        val counter = msgsPerChat.values.sum()

        if (counter == 1) setSingleMessageUI(participant.userId, username, avatar, text) else setMultipleMessagesUI(participant.userId, username, avatar, counter)
    }

    private fun setSingleMessageUI(userId: String, username: String, avatar: Uri, text: String) = with(binding) {
        kaleyraTitle.text = username
        kaleyraMessage.text = text
        kaleyraMessage.maxLines = 2
        kaleyraTime.visibility = View.VISIBLE
        sendersId.add(userId)
        kaleyraAvatars.clean()
        kaleyraAvatars.addAvatar(userId, username, avatar)
    }

    private fun setMultipleMessagesUI(userId: String, username: String, avatar: Uri, counter: Int) = with(binding) {
        kaleyraMessage.text = null
        kaleyraMessage.maxLines = 0
        kaleyraTime.visibility = View.GONE
        kaleyraTitle.text = resources.getString(R.string.kaleyra_glass_new_messages_pattern, counter)
        if (sendersId.contains(userId)) return@with
        sendersId.add(userId)
        kaleyraAvatars.addAvatar(userId, username, avatar)
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
                             }, AUTO_DISMISS_TIME)
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
            TouchEvent.Type.TAP        -> {
                onTap(); true
            }
            TouchEvent.Type.SWIPE_DOWN -> {
                onSwipeDown(); true
            }
            else                       -> false
        }

    private fun AvatarGroupView.addAvatar(userId: String, username: String, avatar: Uri) {
        if (avatar == Uri.EMPTY)
            addAvatar(
                username[0].uppercase(),
                userId.parseToColor()
            )
        else addAvatar(avatar)
    }

    private fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        bottomNavView.setThirdItemListener { onSwipeDown() }
        bottomNavView.setSecondItemListener { onTap() }
    }

    private fun onTap() {
        expandRootView {
            isLayoutExpanded = true
            binding.kaleyraMessage.maxLines = Int.MAX_VALUE
            val chat = getChat(intent)
            chat?.let { KaleyraVideo.conversation.show(this@GlassChatNotificationActivity, it) }
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

    private fun getNOfUnreadMessages(chat: Chat): Int = chat.messages.replayCache[0].other.count { it.state.value is Message.State.Received }

    private fun getLastReceivedMessage(chat: Chat): OtherMessage? = chat.messages.replayCache[0].other.firstOrNull { it.state.value is Message.State.Received }

    private fun getChat(intent: Intent): ChatUI? = intent.extras?.getString("chatId")?.let { chatID ->
        KaleyraVideo.conversation.chats.replayCache.flatten().firstOrNull { it.id == chatID }
    }

    private companion object {
        const val ANIMATION_DURATION = 300L
        const val AUTO_DISMISS_TIME = 3000L
    }
}