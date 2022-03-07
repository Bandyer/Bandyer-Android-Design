/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.chat.notification

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * The notification manager encapsulates all the logic to handle the notifications.
 * It's responsible for attaching/showing a [ChatNotificationView].
 * The notification is automatically detached/hidden after a certain amount of time, if not previously dismissed.
 */
internal class ChatNotificationManager {

    /**
     * Listener to observe the notification state
     */
    interface NotificationListener {
        /**
         * Called when the notification is shown
         */
        fun onShow()

        /**
         * Called when the notification is fully expanded
         */
        fun onExpanded()

        /**
         * Called when the notification is dimissed
         */
        fun onDismiss()
    }

    /**
     * State of the notification
     */
    private enum class State {
        /**
         * c o l l a p s e d
         */
        COLLAPSED,

        /**
         * e x p a n d e d
         */
        EXPANDED,

        /**
         * h i d d e n
         */
        HIDDEN
    }

    /**
     * Do Not Disturb flag. If set to true, the notifications are no longer shown.
     */
    var dnd: Boolean = false

    private var notificationView: ChatNotificationView? = null
    private var attacherChatNotification: ChatNotificationViewAttacher? = null
    private var listeners = arrayListOf<NotificationListener>()

    private var state: State = State.HIDDEN

    private constructor(viewGroup: ViewGroup) {
        notificationView = ChatNotificationView(viewGroup.context)
        notificationView?.setNavigationBarOnClickListeners({ dismiss() }, { expand() })
        attacherChatNotification =
            ChatNotificationViewAttacher.create(viewGroup, notificationView!!)
    }

    /**
     * @param layout The [ConstraintLayout] to which attach the [ChatNotificationView]
     * @constructor
     */
    constructor(layout: ConstraintLayout) : this(viewGroup = layout)

    /**
     * @param layout The [FrameLayout] to which attach the [ChatNotificationView]
     * @constructor
     */
    constructor(layout: FrameLayout) : this(viewGroup = layout)

    /**
     * @param layout The [RelativeLayout] to which attach the [ChatNotificationView]
     * @constructor
     */
    constructor(layout: RelativeLayout) : this(viewGroup = layout)

    /**
     * Add a listener on the notification events
     *
     * @param listener NotificationListener
     */
    fun addListener(listener: ChatNotificationManager.NotificationListener) {
        listeners.add(listener)
    }

    /**
     * Remove a listener on the notification events
     *
     * @param listener NotificationListener
     */
    fun removeListener(listener: NotificationListener) {
        listeners.remove(listener)
    }

    /**
     * Show the [ChatNotificationView] and automatically hide it after [AUTO_HIDE_DELAY] seconds
     *
     * @param notificationData The data needed for the [notification][ChatNotificationView]
     */
    fun show(notificationData: List<ChatNotificationData>) {
        attacherChatNotification!!.attach()

        if (dnd) return
        state = State.COLLAPSED
        notificationView?.show(notificationData)
        listeners.forEach { it.onShow() }

        notificationView?.postDelayed({
            if (state != State.COLLAPSED) return@postDelayed
            dismiss()
        }, AUTO_HIDE_DELAY)
    }

    /**
     * Dismiss the [ChatNotificationView] either with or without animation
     *
     * @param withAnimation True to enable the animation, false otherwise
     */
    fun dismiss(withAnimation: Boolean = true) {
        state = State.HIDDEN
        notificationView?.hide(withAnimation) {
            listeners.forEach { it.onDismiss() }
            attacherChatNotification!!.detach()
        }
    }

    /**
     * Expand the [ChatNotificationView]
     */
    fun expand() {
        state = State.EXPANDED
        notificationView?.expand {
            listeners.forEach { it.onExpanded() }
            attacherChatNotification!!.detach()
        }
    }

    private companion object {
        // Delay after which the notification is automatically hidden
        const val AUTO_HIDE_DELAY = 5000L
    }
}