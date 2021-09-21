package com.bandyer.video_android_glass_ui.chat.notification

import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * The notification manager encapsulates all the logic to handle the notifications.
 * It's responsible for attaching/showing a [BandyerChatNotificationView].
 * The notification is automatically detached/hidden after a certain amount of time, if not previously dismissed.
 */
class BandyerNotificationManager {

    interface NotificationListener {
        fun onShow()
        fun onExpanded()
        fun onDismiss()
    }

    enum class State {
        COLLAPSED,
        EXPANDED,
        HIDDEN
    }

    private var notificationView: BandyerChatNotificationView? = null
    private var attacher: BandyerViewAttacher? = null
    private var listeners = arrayListOf<NotificationListener>()

    private var state: State = State.HIDDEN
    var dnd: Boolean = false

    /**
     * @param layout The [ConstraintLayout] to which attach the [BandyerChatNotificationView]
     * @constructor
     */
    constructor(layout: ConstraintLayout) {
        notificationView = BandyerChatNotificationView(layout.context)
        attacher = BandyerConstraintLayoutAttacher(layout, notificationView!!)
    }

    /**
     * @param layout The [FrameLayout] to which attach the [BandyerChatNotificationView]
     * @constructor
     */
    constructor(layout: FrameLayout) {
        notificationView = BandyerChatNotificationView(layout.context)
        attacher = BandyerFrameLayoutAttacher(layout, notificationView!!)
    }

    /**
     * @param layout The [RelativeLayout] to which attach the [BandyerChatNotificationView]
     * @constructor
     */
    constructor(layout: RelativeLayout) {
        notificationView = BandyerChatNotificationView(layout.context)
        attacher = BandyerRelativeLayoutAttacher(layout, notificationView!!)
    }

    /**
     * Add a listener on the notification events
     *
     * @param listener NotificationListener
     */
    fun addListener(listener: NotificationListener) {
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
     * Show the [BandyerChatNotificationView] and automatically hide it after [AUTO_HIDE_DELAY] seconds
     *
     * @param notificationData The data needed for the [notification][BandyerChatNotificationView]
     */
    fun show(notificationData: List<BandyerNotificationData>) {
        attacher!!.attach()

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
     * Dismiss the [BandyerChatNotificationView] either with or without animation
     *
     * @param withAnimation True to enable the animation, false otherwise
     */
    fun dismiss(withAnimation: Boolean = true) {
        state = State.HIDDEN
        notificationView?.hide(withAnimation) {
            listeners.forEach { it.onDismiss() }
            attacher!!.detach()
        }
    }

    /*
     * Expand the [ChatNotificationView]
     */
    fun expand() {
        state = State.EXPANDED
        notificationView?.expand {
            listeners.forEach { it.onExpanded() }
            attacher!!.detach()
        }
    }

    companion object {
        const val AUTO_HIDE_DELAY = 5000L
    }
}