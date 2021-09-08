package com.bandyer.sdk_design.new_smartglass.chat.notification

import android.content.Context
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * The notification manager
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
    private var listener: NotificationListener? = null

    private var state: State = State.HIDDEN

    /**
     * @param layout The [ConstraintLayout] to which attach the [BandyerChatNotificationView]
     * @param listener An optional [NotificationListener]
     * @constructor
     */
    constructor(layout: ConstraintLayout, listener: NotificationListener? = null) {
        init(layout.context, listener)
        attacher!!.attach(layout)
    }

    /**
     * @param layout The [FrameLayout] to which attach the [BandyerChatNotificationView]
     * @param listener An optional [NotificationListener]
     * @constructor
     */
    constructor(layout: FrameLayout, listener: NotificationListener? = null) {
        init(layout.context, listener)
        attacher!!.attach(layout)
    }

    /**
     * @param layout The [RelativeLayout] to which attach the [BandyerChatNotificationView]
     * @param listener An optional [NotificationListener]
     * @constructor
     */
    constructor(layout: RelativeLayout, listener: NotificationListener? = null) {
        init(layout.context, listener)
        attacher!!.attach(layout)
    }

    private fun init(context: Context, listener: NotificationListener? = null) {
        this.listener = listener
        notificationView = BandyerChatNotificationView(context)
        attacher = BandyerViewAttacher(notificationView!!)
    }

    /**
     * Show the [BandyerChatNotificationView] and automatically hide it after [AUTO_HIDE_DELAY] seconds
     *
     * @param notificationData The data needed for the [notification][BandyerChatNotificationView]
     */
    fun show(notificationData: List<BandyerNotificationData>) {
        state = State.COLLAPSED
        notificationView?.show(notificationData)
        listener?.onShow()

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
        notificationView?.hide(withAnimation)
        listener?.onDismiss()
    }

    /*
     * Expand the [ChatNotificationView]
     */
    fun expand() {
        state = State.EXPANDED
        notificationView?.expand {
            listener?.onExpanded()
        }
    }

    companion object {
        const val AUTO_HIDE_DELAY = 5000L
    }
}