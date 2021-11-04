package com.bandyer.video_android_glass_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setAlphaWithAnimation
import com.bandyer.video_android_glass_ui.bottom_navigation.BottomNavigationView
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.utils.TiltFragment

/**
 * BaseFragment. A base class for all the smart glass fragments
 */
internal abstract class BaseFragment : TiltFragment(), TouchEventListener, ChatNotificationManager.NotificationListener {

    /**
     * The [GlassActivity]
     */
    private val activity by lazy { requireActivity() as GlassActivity }

    /**
     * The fragment's view binding
     */
    protected abstract val binding: ViewBinding

    /**
     * Handle the tap event
     *
     * @return Boolean True if the event has been handled, false otherwise
     */
    protected abstract fun onTap(): Boolean

    /**
     * Handle the swipe down event
     *
     * @return Boolean True if the event has been handled, false otherwise
     */
    protected abstract fun onSwipeDown(): Boolean

    /**
     * Handle the swipe forward event
     *
     * @param isKeyEvent True if the event source was a key
     * @return Boolean True if the event has been handled, false otherwise
     */
    protected abstract fun onSwipeForward(isKeyEvent: Boolean): Boolean

    /**
     * Handle the swipe backward event
     *
     * @param isKeyEvent True if the event source was a key
     * @return Boolean True if the event has been handled, false otherwise
     */
    protected abstract fun onSwipeBackward(isKeyEvent: Boolean): Boolean

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity.onDestinationChanged(NavHostFragment.findNavController(this).currentDestination!!.id)
        activity.addNotificationListener(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        activity.removeNotificationListener(this)
    }

    override fun onShow() = binding.root.setAlphaWithAnimation(0f, 100L)

    override fun onExpanded() = Unit

    override fun onDismiss() = binding.root.setAlphaWithAnimation(1f, 100L)

    /**
     * This method should NOT be overridden. Use onTap, onSwipeDown, onSwipeForward, onSwipeBackward instead.
     */
    override fun onTouch(event: TouchEvent) = when (event.type) {
        TouchEvent.Type.TAP             -> onTap()
        TouchEvent.Type.SWIPE_DOWN      -> onSwipeDown()
        TouchEvent.Type.SWIPE_FORWARD   -> onSwipeForward(event.source == TouchEvent.Source.KEY)
        TouchEvent.Type.SWIPE_BACKWARD  -> onSwipeBackward(event.source == TouchEvent.Source.KEY)
        else -> false
    }

    /**
     * Apply onClickListeners for realwear voice commands
     *
     * @receiver BottomNavigationView
     */
    protected fun BottomNavigationView.setListenersForRealwear() {
        setTapOnClickListener { onTap() }
        setSwipeDownOnClickListener { onSwipeDown() }
        setSwipeHorizontalOnClickListener { onSwipeForward(true) }
    }
}