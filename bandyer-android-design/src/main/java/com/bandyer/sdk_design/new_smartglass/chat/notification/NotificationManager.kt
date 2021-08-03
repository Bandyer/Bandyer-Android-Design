package com.bandyer.sdk_design.new_smartglass.chat.notification

import android.content.Context
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout

class NotificationManager {

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

    private var notificationView: ChatNotificationView? = null
    private var attacher: NotificationViewAttacher? = null
    private var listener: NotificationListener? = null

    private var state: State = State.HIDDEN

    constructor(layout: ConstraintLayout, listener: NotificationListener? = null) {
        init(layout.context, listener)
        attacher!!.attach(layout)
    }

    constructor(layout: FrameLayout, listener: NotificationListener? = null) {
        init(layout.context, listener)
        attacher!!.attach(layout)
    }

    constructor(layout: RelativeLayout, listener: NotificationListener? = null) {
        init(layout.context, listener)
        attacher!!.attach(layout)
    }

    private fun init(context: Context, listener: NotificationListener? = null) {
        this.listener = listener
        notificationView = ChatNotificationView(context)
        attacher = NotificationViewAttacher(notificationView!!)
    }

    fun show(notificationData: List<NotificationData>) {
        state = State.COLLAPSED
        notificationView?.show(notificationData)
        listener?.onShow()

//        notificationView?.postDelayed({
//            if (state != State.COLLAPSED) return@postDelayed
//            dismiss()
//        }, AUTO_HIDE_DELAY)
    }

    fun dismiss(withAnimation: Boolean = true) {
        state = State.HIDDEN
        notificationView?.hide(withAnimation)
        listener?.onDismiss()
    }

    fun expand() {
        state = State.EXPANDED
        notificationView?.expand {
            listener?.onExpanded()
        }
    }

    companion object {
        const val AUTO_HIDE_DELAY = 2000L
    }
}