package com.kaleyra.collaboration_suite_phone_ui.utils

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.customview.widget.ViewDragHelper.Callback
import kotlin.math.max
import kotlin.math.min


/**
 * It's a [Behavior](https://developer.android.com/reference/android/support/design/widget/CoordinatorLayout.Behavior) that allows you to swipe a view off the screen
 * @suppress
 */
internal class SwipeDismissBehavior2<V : View> : Behavior<V>() {
    /**
     * @hide
     */
    @IntDef(SWIPE_DIRECTION_START_TO_END, SWIPE_DIRECTION_END_TO_START, SWIPE_DIRECTION_ANY)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    private annotation class SwipeDirection

    private var mViewDragHelper: ViewDragHelper? = null
    private var mListener: OnDismissListener? = null
    private var mIgnoreEvents = false
    private var mSensitivity = 0f
    private var mSensitivitySet = false
    private var mSwipeDirection = SWIPE_DIRECTION_ANY
    private var mDragDismissThreshold = DEFAULT_DRAG_DISMISS_THRESHOLD
    private var mAlphaStartSwipeDistance = DEFAULT_ALPHA_START_DISTANCE
    private var mAlphaEndSwipeDistance = DEFAULT_ALPHA_END_DISTANCE

    /**
     * Callback interface used to notify the application that the view has been dismissed.
     */
    interface OnDismissListener {
        /**
         * Called when `view` has been dismissed via swiping.
         */
        fun onDismiss(view: View?)

        /**
         * Called when the drag state has changed.
         *
         * @param state the new state. One of
         * [.STATE_IDLE], [.STATE_DRAGGING] or [.STATE_SETTLING].
         */
        fun onDragStateChanged(state: Int)
    }

    /**
     * Set the listener to be used when a dismiss event occurs.
     *
     * @param listener the listener to use.
     */
    fun setListener(listener: OnDismissListener?) {
        mListener = listener
    }

    /**
     * Sets the swipe direction for this behavior.
     *
     * @param direction one of the [.SWIPE_DIRECTION_START_TO_END],
     * [.SWIPE_DIRECTION_END_TO_START] or [.SWIPE_DIRECTION_ANY]
     */
    fun setSwipeDirection(@SwipeDirection direction: Int) {
        mSwipeDirection = direction
    }

    /**
     * Set the threshold for telling if a view has been dragged enough to be dismissed.
     *
     * @param distance a ratio of a view's width, values are clamped to 0 >= x <= 1f;
     */
    fun setDragDismissDistance(distance: Float) {
        mDragDismissThreshold = clamp(0f, distance, 1f)
    }

    /**
     * The minimum swipe distance before the view's alpha is modified.
     *
     * @param fraction the distance as a fraction of the view's width.
     */
    fun setStartAlphaSwipeDistance(fraction: Float) {
        mAlphaStartSwipeDistance = clamp(0f, fraction, 1f)
    }

    /**
     * The maximum swipe distance for the view's alpha is modified.
     *
     * @param fraction the distance as a fraction of the view's width.
     */
    fun setEndAlphaSwipeDistance(fraction: Float) {
        mAlphaEndSwipeDistance = clamp(0f, fraction, 1f)
    }

    /**
     * Set the sensitivity used for detecting the start of a swipe. This only takes effect if
     * no touch handling has occured yet.
     *
     * @param sensitivity Multiplier for how sensitive we should be about detecting
     * the start of a drag. Larger values are more sensitive. 1.0f is normal.
     */
    fun setSensitivity(sensitivity: Float) {
        mSensitivity = sensitivity
        mSensitivitySet = true
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        when (MotionEventCompat.getActionMasked(event)) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->                 // Reset the ignore flag
                if (mIgnoreEvents) {
                    mIgnoreEvents = false
                    return false
                }
            else                                             -> mIgnoreEvents = !parent.isPointInChildBounds(
                child,
                event.x.toInt(), event.y.toInt()
            )
        }
        if (mIgnoreEvents) {
            return false
        }
        ensureViewDragHelper(parent)
        return mViewDragHelper!!.shouldInterceptTouchEvent(event)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (mViewDragHelper != null) {
            mViewDragHelper!!.processTouchEvent(event)
            return true
        }
        return false
    }

    private val mDragCallback: Callback = object : Callback() {
        private var mOriginalCapturedViewLeft = 0
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            mOriginalCapturedViewLeft = child.left
            return true
        }

        override fun onViewDragStateChanged(state: Int) {
            if (mListener != null) {
                mListener!!.onDragStateChanged(state)
            }
        }

        override fun onViewReleased(child: View, xvel: Float, yvel: Float) {
            val childWidth = child.width
            val targetLeft: Int
            var dismiss = false
            if (shouldDismiss(child, xvel)) {
                targetLeft = if (child.left < mOriginalCapturedViewLeft) mOriginalCapturedViewLeft - childWidth else mOriginalCapturedViewLeft + childWidth
                dismiss = true
            } else {
                // Else, reset back to the original left
                targetLeft = mOriginalCapturedViewLeft
            }
            if (mViewDragHelper!!.settleCapturedViewAt(targetLeft, child.top)) {
                ViewCompat.postOnAnimation(child, SettleRunnable(child, dismiss))
            } else if (dismiss) {
                child.visibility = View.GONE
                if (mListener != null) {
                    mListener!!.onDismiss(child)
                }
            }
        }

        private fun shouldDismiss(child: View, xvel: Float): Boolean {
            if (xvel != 0f) {
                val isRtl = (ViewCompat.getLayoutDirection(child)
                        == ViewCompat.LAYOUT_DIRECTION_RTL)
                if (mSwipeDirection == SWIPE_DIRECTION_ANY) {
                    // We don't care about the direction so return true
                    return true
                } else if (mSwipeDirection == SWIPE_DIRECTION_START_TO_END) {
                    // We only allow start-to-end swiping, so the fling needs to be in the
                    // correct direction
                    return if (isRtl) xvel < 0f else xvel > 0f
                } else if (mSwipeDirection == SWIPE_DIRECTION_END_TO_START) {
                    // We only allow end-to-start swiping, so the fling needs to be in the
                    // correct direction
                    return if (isRtl) xvel > 0f else xvel < 0f
                }
            } else {
                val distance = child.left - mOriginalCapturedViewLeft
                val thresholdDistance = Math.round(child.width * mDragDismissThreshold)
                return Math.abs(distance) >= thresholdDistance
            }
            return false
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return child.width
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val isRtl = (ViewCompat.getLayoutDirection(child)
                    == ViewCompat.LAYOUT_DIRECTION_RTL)
            val min: Int
            val max: Int
            if (mSwipeDirection == SWIPE_DIRECTION_START_TO_END) {
                if (isRtl) {
                    min = mOriginalCapturedViewLeft - child.width
                    max = mOriginalCapturedViewLeft
                } else {
                    min = mOriginalCapturedViewLeft
                    max = mOriginalCapturedViewLeft + child.width
                }
            } else if (mSwipeDirection == SWIPE_DIRECTION_END_TO_START) {
                if (isRtl) {
                    min = mOriginalCapturedViewLeft
                    max = mOriginalCapturedViewLeft + child.width
                } else {
                    min = mOriginalCapturedViewLeft - child.width
                    max = mOriginalCapturedViewLeft
                }
            } else {
                min = mOriginalCapturedViewLeft - child.width
                max = mOriginalCapturedViewLeft + child.width
            }
            return clamp(min, left, max)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return child.top
        }

        override fun onViewPositionChanged(child: View, left: Int, top: Int, dx: Int, dy: Int) {
            val distance = fraction(0f, child.width.toFloat(), Math.abs(left - mOriginalCapturedViewLeft).toFloat())
            child.alpha = clamp(0f, 1f - distance, 1f)
        }
    }

    private fun ensureViewDragHelper(parent: ViewGroup) {
        if (mViewDragHelper == null) {
            mViewDragHelper = if (mSensitivitySet) ViewDragHelper.create(parent, mSensitivity, mDragCallback) else ViewDragHelper.create(parent, mDragCallback)
        }
    }

    private inner class SettleRunnable internal constructor(private val mView: View, private val mDismiss: Boolean) : Runnable {
        override fun run() {
            if (mViewDragHelper != null && mViewDragHelper!!.continueSettling(true)) {
                ViewCompat.postOnAnimation(mView, this)
            } else {
                if (mDismiss) {
                    mView.visibility = View.GONE
                    if (mListener != null) {
                        mListener!!.onDismiss(mView)
                    }
                }
            }
        }
    }

    /**
     * Retrieve the current drag state of this behavior. This will return one of
     * [.STATE_IDLE], [.STATE_DRAGGING] or [.STATE_SETTLING].
     *
     * @return The current drag state
     */
    val dragState: Int
        get() = if (mViewDragHelper != null) mViewDragHelper!!.viewDragState else STATE_IDLE

    companion object {
        /**
         * A view is not currently being dragged or animating as a result of a fling/snap.
         */
        const val STATE_IDLE = ViewDragHelper.STATE_IDLE

        /**
         * A view is currently being dragged. The position is currently changing as a result
         * of user input or simulated user input.
         */
        const val STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING

        /**
         * A view is currently settling into place as a result of a fling or
         * predefined non-interactive motion.
         */
        const val STATE_SETTLING = ViewDragHelper.STATE_SETTLING

        /**
         * Swipe direction that only allows swiping in the direction of start-to-end. That is
         * left-to-right in LTR, or right-to-left in RTL.
         */
        const val SWIPE_DIRECTION_START_TO_END = 0

        /**
         * Swipe direction that only allows swiping in the direction of end-to-start. That is
         * right-to-left in LTR or left-to-right in RTL.
         */
        const val SWIPE_DIRECTION_END_TO_START = 1

        /**
         * Swipe direction which allows swiping in either direction.
         */
        const val SWIPE_DIRECTION_ANY = 2
        private const val DEFAULT_DRAG_DISMISS_THRESHOLD = 0.5f
        private const val DEFAULT_ALPHA_START_DISTANCE = 0f
        private const val DEFAULT_ALPHA_END_DISTANCE = DEFAULT_DRAG_DISMISS_THRESHOLD

        private fun clamp(min: Float, value: Float, max: Float): Float = min(max(min, value), max)
        private fun clamp(min: Int, value: Int, max: Int): Int = min(max(min, value), max)

        /**
         * The fraction that `value` is between `startValue` and `endValue`.
         */
        fun fraction(startValue: Float, endValue: Float, value: Float): Float = (value - startValue) / (endValue - startValue)
    }
}