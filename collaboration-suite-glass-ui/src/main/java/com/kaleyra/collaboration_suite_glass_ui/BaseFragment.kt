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

package com.kaleyra.collaboration_suite_glass_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.kaleyra.collaboration_suite_core_ui.common.BoundServiceActivity
import com.kaleyra.collaboration_suite_core_ui.extensions.ViewExtensions.setAlphaWithAnimation
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.chat.notification.ChatNotificationManager
import com.kaleyra.collaboration_suite_glass_ui.utils.TiltFragment

/**
 * BaseFragment. A base class for all the smart glass fragments
 */
internal abstract class BaseFragment : TiltFragment(), TouchEventListener,
    ChatNotificationManager.NotificationListener,
    BoundServiceActivity.Observer {

    /**
     * The [GlassCallActivity]
     */
    private val activity
        get() = requireActivity() as GlassCallActivity

    /**
     * Flag which point outs if the call service is already bound
     */
    private val isServiceBound: Boolean
        get() = activity.isServiceBound

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
        activity.addNotificationListener(this)
        activity.addServiceBoundObserver(this)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * @suppress
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NavHostFragment.findNavController(this).currentDestination?.id?.also {
            activity.onDestinationChanged(it)
        }
        if (isServiceBound)
            onServiceBound()
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        activity.removeServiceBoundObserver(this)
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