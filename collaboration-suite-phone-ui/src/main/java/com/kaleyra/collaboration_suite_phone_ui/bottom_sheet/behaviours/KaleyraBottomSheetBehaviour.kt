/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.behaviours

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper

import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutContent
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs
import kotlin.math.max

/**
 * BottomSheet Behaviour
 * @suppress
 * @constructor
 * TODO: remove in favor or material behaviour
 */
@SuppressLint("PrivateResource")
class KaleyraBottomSheetBehaviour<V : View>(context: Context, attrs: AttributeSet?) : CoordinatorLayout.Behavior<V>(context, attrs) {

    /**
     * The height of the bottom sheet when it is collapsed.
     *
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_peekHeight
     */
    var peekHeight: Int = 0
        set(peekHeight) {
            field = max(0, peekHeight)
            mMaxOffset = mParentHeight - peekHeight
        }

    var anchorOffset: Int = -1
        set(anchorOffset) {
            field = anchorOffset
            skipAnchor = anchorOffset < 0
            mAnchorPoint = if (anchorOffset < 0) 0 else mParentHeight - anchorOffset
        }

    /**
     * Whether this bottom sheet can hide when it is swiped down.
     *
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    var isHideable: Boolean = false

    /**
     * Whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once.
     *
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    var skipCollapsed: Boolean = false

    /**
     * Sets whether this bottom sheet should skip the anchor state when it is being hidden
     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipAnchored
     */
    var skipAnchor: Boolean = true

    @State
    private var mState = STATE_HIDDEN

    @State
    var lastStableState = STATE_HIDDEN

    private var mViewDragHelper: ViewDragHelper? = null

    private var mIgnoreEvents: Boolean = false

    private var mNestedScrolled: Boolean = false

    private var mParentHeight: Int = 0
        get() = if (field == 0) 5000 else field

    private var mViewRef: WeakReference<V>? = null

    private var mNestedScrollingChildRef: WeakReference<View>? = null

    private var mCallback: Vector<BottomSheetCallback>? = null

    private var mActivePointerId: Int = 0

    private var mInitialY: Int = 0

    private var mTouchingScrollingChild: Boolean = false

    private var mVelocityTracker: VelocityTracker? = null

    private val mScrollVelocityTracker = ScrollVelocityTracker()

    private val mMinimumVelocity: Float

    private val mMaximumVelocity: Float

    private var mMinOffset: Int = 0

    private var mMaxOffset: Int = 0

    private var mAnchorPoint: Int = 0

    private var mViewClickedTop: Int = -1

    private var slideOffset: Float = 0f
    private var orientation: Int = Configuration.ORIENTATION_PORTRAIT

    var disableDragging = false

    /**
     * The state of the bottom sheet.
     *
     * @return One of [.STATE_EXPANDED], [.STATE_ANCHOR_POINT], [.STATE_COLLAPSED],
     * [.STATE_DRAGGING], and [.STATE_SETTLING].
     */
    @State
    var state: Int
        get() = mState
        set(state) {

            if (mViewRef == null) {
                if (state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_ANCHOR_POINT || isHideable && state == STATE_HIDDEN) {
                    mState = state
                    lastStableState = state
                }
                return
            }

            val child = mViewRef!!.get() ?: return

            val top = when {
                !skipAnchor && state == STATE_ANCHOR_POINT -> mAnchorPoint
                state == STATE_COLLAPSED                   -> mMaxOffset
                state == STATE_EXPANDED                    -> mMinOffset
                isHideable && state == STATE_HIDDEN        -> mParentHeight
                else                                       -> throw IllegalArgumentException("Illegal state argument: $state")
            }

            if (state != mState)
                setStateInternal(STATE_SETTLING)

            if (mViewDragHelper!!.smoothSlideViewTo(child, child.left, top))
                ViewCompat.postOnAnimation(child, SettleRunnable(child, state))
            else
                setStateInternal(state)
        }

    private val mDragCallback = object : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            when {
                mState == STATE_DRAGGING || mTouchingScrollingChild       -> return true
                mState == STATE_EXPANDED && mActivePointerId == pointerId -> // Let the content scroll up
                    if (mNestedScrollingChildRef?.get()?.canScrollVertically(-1) == true)
                        return true
            }
            return mViewRef?.get() == child
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dispatchOnSlide(top)
        }

        override fun onViewDragStateChanged(state: Int) {
            if (!disableDragging && state == ViewDragHelper.STATE_DRAGGING)
                setStateInternal(STATE_DRAGGING)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (disableDragging)
                return

            var top: Int = releasedChild.top
            @State val targetState: Int

            if (yvel < MIN_VELOCITY && yvel != 0f) { // Moving up
                if (!skipAnchor && yvel > -MIN_VELOCITY_SKIP_ANCHOR_STATE && lastStableState == STATE_COLLAPSED) {
                    top = mAnchorPoint
                    targetState = STATE_ANCHOR_POINT
                } else {
                    top = mMinOffset
                    targetState = STATE_EXPANDED
                }
            } else if (yvel > MIN_VELOCITY && yvel != 0f) {
                if (!skipAnchor && yvel < MAX_VELOCITY_SKIP_ANCHOR_STATE && lastStableState == STATE_EXPANDED) {
                    top = mAnchorPoint
                    targetState = STATE_ANCHOR_POINT
                } else if (!skipCollapsed) {
                    top = mMaxOffset
                    targetState = STATE_COLLAPSED
                } else if (!skipAnchor) {
                    top = mAnchorPoint
                    targetState = STATE_ANCHOR_POINT
                } else if (isHideable && shouldHide(releasedChild, yvel)) {
                    top = mParentHeight
                    targetState = STATE_HIDDEN
                } else {
                    top = when (lastStableState) {
                        STATE_COLLAPSED    -> mMaxOffset
                        STATE_ANCHOR_POINT -> mAnchorPoint
                        else               -> mMinOffset
                    }
                    targetState = if (lastStableState == STATE_HIDDEN) STATE_EXPANDED else lastStableState
                }
            } else {
                if (abs(top - mMinOffset) < abs(top - mMaxOffset)) {
                    top = mMinOffset
                    targetState = STATE_EXPANDED
                } else if (!skipCollapsed) {
                    top = mMaxOffset
                    targetState = STATE_COLLAPSED
                } else if (isHideable && shouldHide(releasedChild, yvel)) {
                    top = mParentHeight
                    targetState = STATE_HIDDEN
                } else {
                    top = when (lastStableState) {
                        STATE_COLLAPSED    -> mMaxOffset
                        STATE_ANCHOR_POINT -> mAnchorPoint
                        else               -> mMinOffset
                    }
                    targetState = if (lastStableState == STATE_HIDDEN) STATE_EXPANDED else lastStableState
                }
            }

            if (mViewDragHelper!!.settleCapturedViewAt(releasedChild.left, top)) {
                setStateInternal(STATE_SETTLING)
                ViewCompat.postOnAnimation(releasedChild, SettleRunnable(releasedChild, targetState))
            } else {
                setStateInternal(targetState)
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return constrain(top, mMinOffset, if (isHideable) mParentHeight else mMaxOffset)
        }

        fun constrain(amount: Int, low: Int, high: Int): Int {
            return if (amount < low) low else if (amount > high) high else amount
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return if (isHideable) {
                mParentHeight - mMinOffset
            } else {
                mMaxOffset - mMinOffset
            }
        }
    }

    /**
     * Callback for monitoring events about bottom sheets.
     */
    abstract class BottomSheetCallback {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param newState    The new state. This will be one of [.STATE_DRAGGING],
         * [.STATE_SETTLING], [.STATE_ANCHOR_POINT],
         * [.STATE_EXPANDED],
         * [.STATE_COLLAPSED], or [.STATE_HIDDEN].
         */
        abstract fun onStateChanged(bottomSheet: View, @State newState: Int)

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within its range, from 0 to 1
         * when it is moving upward, and from 0 to -1 when it moving downward.
         */
        abstract fun onSlide(bottomSheet: View, slideOffset: Float)


        /**
         * Called when the bottom sheet was initialized.
         *
         * @param bottomSheet The bottom sheet view.
         * @param state The new state. This will be one of [.STATE_DRAGGING],
         * [.STATE_SETTLING], [.STATE_ANCHOR_POINT],
         * [.STATE_EXPANDED],
         * [.STATE_COLLAPSED], or [.STATE_HIDDEN].
         * @param slideOffset Offset range between [0 - 1.0]
         */
        abstract fun onDrawn(bottomSheet: View, @State state: Int, @FloatRange(from = 0.0, to = 1.0) slideOffset: Float)
    }


    /**
     * @hide
     */
    @IntDef(STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_ANCHOR_POINT, STATE_SETTLING, STATE_HIDDEN)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State
    @Suppress("unused")
    constructor()

    init {
        var a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetBehavior_Layout)
        peekHeight = a.getDimensionPixelSize(R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, 0)
        isHideable = a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false)
        a.recycle()
        a = context.obtainStyledAttributes(attrs, R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Behavior)
        if (attrs != null) {
            mAnchorPoint = a.getDimension(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Behavior_kaleyra_anchorPoint, -1f).toInt()
            skipAnchor = mAnchorPoint < 0
            mState = a.getInt(R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Behavior_kaleyra_defaultStatus, STATE_COLLAPSED)
        }
        a.recycle()
        val configuration = ViewConfiguration.get(context)
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
    }

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: V): Parcelable? {
        val state = super.onSaveInstanceState(parent, child) ?: return null
        return SavedState(state, mState)
    }

    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: V, state: Parcelable) {
        val ss = state as SavedState?
        super.onRestoreInstanceState(parent, child, ss!!.superState)
        // Intermediate states are restored as collapsed state
        mState = when {
            ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING -> STATE_COLLAPSED
            else                                                     -> ss.state
        }

        lastStableState = mState
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child))
            child.fitsSystemWindows = true

        if (child !is BottomSheetLayoutContent)
            return false

        val savedTop = child.top
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection)

        // Offset the bottom sheet
        mParentHeight = parent.height
        mMinOffset = max(0, mParentHeight - child.height)
        mMaxOffset = max(mParentHeight - peekHeight, mMinOffset)
        mAnchorPoint = mParentHeight - anchorOffset

        /**
         * New behavior
         */
        when (mState) {
            STATE_ANCHOR_POINT             -> ViewCompat.offsetTopAndBottom(child, mAnchorPoint)
            STATE_EXPANDED                 -> ViewCompat.offsetTopAndBottom(child, mMinOffset)
            STATE_HIDDEN                   -> ViewCompat.offsetTopAndBottom(child, mParentHeight)
            STATE_COLLAPSED                -> ViewCompat.offsetTopAndBottom(child, mMaxOffset)
            STATE_DRAGGING, STATE_SETTLING -> ViewCompat.offsetTopAndBottom(child, savedTop - child.top)
        }

        notifyOnDrawn(child)

        if (mViewDragHelper == null)
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)

        mViewRef = WeakReference(child)
        mNestedScrollingChildRef = WeakReference<View>(findScrollingChild(child))
        return true
    }

    private fun isVisible(view: View?): Boolean {
        return view != null && view.isShown && mParentHeight > view.top
    }

    private fun notifyOnDrawn(child: View) {
        val slideOffset = if (child.top > mMaxOffset) (mMaxOffset - child.top).toFloat() / peekHeight
        else (mMaxOffset - child.top).toFloat() / (mMaxOffset - mMinOffset)

        val currentOrientation = child.context.resources.configuration.orientation
        if (this.slideOffset != slideOffset || orientation != currentOrientation) {
            this.slideOffset = slideOffset
            this.orientation = currentOrientation
            mCallback?.forEach {
                val bottomSheet = child as? View ?: return@forEach
                it.onDrawn(bottomSheet, mState, slideOffset)
            }
        }
    }

    fun getStableStateSlideOffset(state: Int): Float {
        return when (state) {
            STATE_ANCHOR_POINT -> (mMaxOffset - mAnchorPoint).toFloat() / (mMaxOffset - mMinOffset)
            STATE_EXPANDED     -> 1f
            else               -> 0f
        }
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        runCatching {
            if (disableDragging || event.pointerCount > 1)
                return false

            val action = event.actionMasked

            if (!isVisible(child)) {
                mIgnoreEvents = true
                return false
            }

            if (action == MotionEvent.ACTION_DOWN) reset()

            if (child is BottomSheetLayoutContent)
                mViewClickedTop = child.top

            // Record the velocity
            if (mVelocityTracker == null)
                mVelocityTracker = VelocityTracker.obtain()

            mVelocityTracker!!.addMovement(event)

            when (action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mTouchingScrollingChild = false
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID

                    // Reset the ignore flag
                    if (mIgnoreEvents) {
                        mIgnoreEvents = false
                        return false
                    }
                }
                MotionEvent.ACTION_DOWN                          -> {
                    mInitialY = event.y.toInt()
                    val scroll = if (mNestedScrollingChildRef != null) mNestedScrollingChildRef!!.get() else null
                    if (scroll != null && isVisible(child)) {
                        mActivePointerId = event.getPointerId(event.actionIndex)
                        mTouchingScrollingChild = true
                    }
                    mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID && !isVisible(child)
                }
            }

            if (mViewDragHelper!!.capturedView != null && mViewDragHelper!!.capturedView !is BottomSheetLayoutContent)
                return false

            if (!mIgnoreEvents && mViewDragHelper != null && mViewDragHelper!!.shouldInterceptTouchEvent(event)) {
                return true
            }

            // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
            // it is not the top most view of its parent. This is not necessary when the touch event is
            // happening over the scrolling content as nested scrolling logic handles that case.
            val scroll = mNestedScrollingChildRef?.get()
            return action == MotionEvent.ACTION_MOVE &&
                    scroll != null &&
                    !mIgnoreEvents &&
                    mState != STATE_DRAGGING &&
                    !isVisible(child) &&
                    mViewDragHelper != null &&
                    abs(mInitialY - event.y) > mViewDragHelper!!.touchSlop
        }
        return false
    }

    private val clickGestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
        override fun onShowPress(e: MotionEvent) {

        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return e.pointerCount == 1 && (mViewRef?.get()?.parent as? ViewGroup)?.performClick() == true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            return false
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent) {

        }

    })

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        runCatching {
            clickGestureDetector.onTouchEvent(event)
            if (disableDragging || event.pointerCount > 1)
                return false

            val action = event.actionMasked

            if (mViewDragHelper?.capturedView is BottomSheetLayoutContent)
                mViewDragHelper!!.processTouchEvent(event)

            if (!isVisible(child))
                return false

            if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN)
                return true

            if (child is BottomSheetLayoutContent)
                mViewClickedTop = child.top

            // Record the velocity
            if (action == MotionEvent.ACTION_DOWN) reset()

            if (mVelocityTracker == null)
                mVelocityTracker = VelocityTracker.obtain()

            mVelocityTracker!!.addMovement(event)
            // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
            // to capture the bottom sheet in case it is not captured and the touch slop is passed.


            if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
                if (abs(mInitialY - event.y) > mViewDragHelper!!.touchSlop && child is BottomSheetLayoutContent) {
                    mViewDragHelper!!.captureChildView(child, event.getPointerId(event.actionIndex))
                }
            }
            return false
        }
        return false
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: V,
        directTargetChild: View, target: View, nestedScrollAxes: Int,
        @ViewCompat.NestedScrollType type: Int
    ): Boolean {
        if (disableDragging)
            return false

        mNestedScrolled = false
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    private inner class ScrollVelocityTracker {

        private var mPreviousScrollTime: Long = 0

        var scrollVelocity = 0f
            private set

        fun recordScroll(dy: Int) {
            val now = System.currentTimeMillis()

            if (mPreviousScrollTime != 0L) {
                val elapsed = now - mPreviousScrollTime
                scrollVelocity = dy.toFloat() / elapsed * 1000 // pixels per sec
            }

            mPreviousScrollTime = now
        }

        fun clear() {
            mPreviousScrollTime = 0
            scrollVelocity = 0f
        }
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout, child: V, target: View,
        dx: Int, dy: Int, consumed: IntArray,
        @ViewCompat.NestedScrollType type: Int
    ) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            // Ignore fling here. The ViewDragHelper handles it.
            return
        }
        val scrollingChild = mNestedScrollingChildRef?.get()
        if (target != scrollingChild) return

        mScrollVelocityTracker.recordScroll(dy)

        val currentTop = child.top
        val newTop = currentTop - dy

        // Force stop at the anchor - do not go from collapsed to expanded in one scroll
        if (!skipAnchor && lastStableState == STATE_COLLAPSED && newTop < mAnchorPoint || lastStableState == STATE_EXPANDED && newTop > mAnchorPoint) {
            consumed[1] = dy
            ViewCompat.offsetTopAndBottom(child, mAnchorPoint - currentTop)
            dispatchOnSlide(child.top)
            mNestedScrolled = true
            return
        }

        if (dy > 0) { // Upward
            if (newTop <= mMinOffset) {
                consumed[1] = currentTop - mMinOffset
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
                setStateInternal(STATE_EXPANDED)
            } else {
                if (disableDragging) {
                    // Prevent dragging
                    return
                }
                consumed[1] = dy
                ViewCompat.offsetTopAndBottom(child, -dy)
                setStateInternal(STATE_DRAGGING)
            }
        } else if (dy < 0) { // Downward
            if (!target.canScrollVertically(-1)) {
                if (newTop <= mMaxOffset || isHideable) {
                    if (disableDragging) {
                        // Prevent dragging
                        return
                    }
                    consumed[1] = dy
                    ViewCompat.offsetTopAndBottom(child, -dy)
                    setStateInternal(STATE_DRAGGING)
                } else if(!skipCollapsed || !skipAnchor) {
                    consumed[1] = currentTop - mMaxOffset
                    ViewCompat.offsetTopAndBottom(child, -consumed[1])
                    if (!skipCollapsed) setStateInternal(STATE_COLLAPSED)
                    if (!skipAnchor) setStateInternal(STATE_ANCHOR_POINT)
                }
            }
        }
        dispatchOnSlide(child.top)
        mNestedScrolled = true
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: V, target: View,
        @ViewCompat.NestedScrollType type: Int
    ) {

        if (child.top == mMinOffset) {
            if(lastStableState != STATE_EXPANDED) setStateInternal(STATE_EXPANDED)
            return
        }

        if (mNestedScrollingChildRef == null || target != mNestedScrollingChildRef?.get() || !mNestedScrolled)
            return

        var top: Int = child.top
        var targetState: Int = lastStableState

        // Are we flinging up?
        val scrollVelocity = mScrollVelocityTracker.scrollVelocity
        mScrollVelocityTracker.clear()

        if (scrollVelocity > mMinimumVelocity) {
            if (!skipAnchor && lastStableState == STATE_COLLAPSED) {
                // Fling from collapsed to anchor
                top = mAnchorPoint
                targetState = STATE_ANCHOR_POINT
            } else {
                top = mMinOffset
                targetState = STATE_EXPANDED
            }
        } else if (scrollVelocity < -mMinimumVelocity) { // Are we flinging down?
            if (!skipAnchor && lastStableState == STATE_EXPANDED) {
                // Fling to from expanded to anchor
                top = mAnchorPoint
                targetState = STATE_ANCHOR_POINT
            } else if (!skipCollapsed) {
                top = mMaxOffset
                targetState = STATE_COLLAPSED
            } else if (isHideable) {
                top = mParentHeight
                targetState = STATE_HIDDEN
            }
        } else {
            // Collapse? Multiply by 1.25 to account for parallax. The currentTop needs to be pulled down 50% of the anchor point before collapsing.
            if (skipAnchor) when {
                !skipCollapsed && top < mMinOffset -> {
                    top = mMaxOffset
                    targetState = STATE_COLLAPSED
                }
                else                               -> {
                    top = mMinOffset
                    targetState = STATE_EXPANDED
                }
            } else when { // Expand?
                top > mAnchorPoint * 1.25 && !skipCollapsed -> { // Multiply by 1.25 to account for parallax. The currentTop needs to be pulled down 50% of the anchor point before collapsing.
                    top = mMaxOffset
                    targetState = STATE_COLLAPSED
                }
                top < mAnchorPoint * 0.5  -> {
                    top = mMinOffset
                    targetState = STATE_EXPANDED
                }
                !skipAnchor                      -> {
                    top = mAnchorPoint
                    targetState = STATE_ANCHOR_POINT
                }
            }
        }

        // Not flinging, just settle to the nearest state

        if (mViewDragHelper!!.smoothSlideViewTo(child, child.left, top)) {
            setStateInternal(STATE_SETTLING)
            ViewCompat.postOnAnimation(child, SettleRunnable(child, targetState))
        } else {
            setStateInternal(targetState)
        }
        mNestedScrolled = false
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout, child: V, target: View,
        velocityX: Float, velocityY: Float
    ): Boolean {
        if (disableDragging)
            return false

        return target === mNestedScrollingChildRef?.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(
            coordinatorLayout, child, target,
            velocityX, velocityY
        ))
    }

    /**
     * Adds a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    fun addBottomSheetCallback(callback: BottomSheetCallback) {
        if (mCallback == null)
            mCallback = Vector()

        mCallback!!.add(callback)
    }

    /**
     * Adds a callback to be notified of bottom sheet events.
     */
    fun removeBottomSheetCallbacks() {
        mCallback?.removeAllElements()
    }

    /**
     * Adds a callback to be notified of bottom sheet events.
     * @param callback to remove
     */
    fun removeBottomSheetCallback(callback: BottomSheetCallback) {
        mCallback?.remove(callback)
    }


    private fun setStateInternal(@State state: Int) {
        if (mState == state)
            return

        if (state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_ANCHOR_POINT || isHideable && state == STATE_HIDDEN)
            lastStableState = state

        mState = state

        mViewRef?.get()?.let {
            notifyStateChangedToListeners(it, state)
        }
    }

    private fun notifyStateChangedToListeners(bottomSheet: View, @State newState: Int) {
        mCallback?.forEach { bottomSheetCallback ->
            bottomSheetCallback.onStateChanged(bottomSheet, newState)
        }
    }

    private fun notifyOnSlideToListeners(bottomSheet: View, slideOffset: Float) {
        mCallback?.forEach { bottomSheetCallback ->
            bottomSheetCallback.onSlide(bottomSheet, slideOffset)
        }
    }

    private fun reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER
        mVelocityTracker?.let {
            it.recycle()
            mVelocityTracker = null
        }
    }

    private fun shouldHide(child: View, yvel: Float): Boolean {
        if (skipCollapsed)
            return true
        // It should not hide, but collapse.
        if (child.top < mMaxOffset)
            return false

        val newTop = child.top + yvel * HIDE_FRICTION
        return abs(newTop - mMaxOffset) / peekHeight.toFloat() > HIDE_THRESHOLD
    }

    private fun findScrollingChild(view: View): View? {
        if (view is NestedScrollingChild)
            return view

        if (view is ViewGroup) {
            var i = 0
            val count = view.childCount
            while (i < count) {
                val scrollingChild = findScrollingChild(view.getChildAt(i))
                if (scrollingChild != null)
                    return scrollingChild
                i++
            }
        }
        return null
    }

    private fun dispatchOnSlide(top: Int) {
        mViewRef?.get()?.let {
            when {
                top > mMaxOffset -> notifyOnSlideToListeners(it, (mMaxOffset - top).toFloat() / peekHeight)
                else             -> notifyOnSlideToListeners(it, (mMaxOffset - top).toFloat() / (mMaxOffset - mMinOffset))
            }
        }
    }

    private inner class SettleRunnable(
        private val mView: View,
        private val mTargetState: Int
    ) : Runnable {
        override fun run() {
            if (mViewDragHelper!!.continueSettling(true))
                ViewCompat.postOnAnimation(mView, this)
            else {
                if (mState != mTargetState)
                    setStateInternal(mTargetState)

                notifyOnDrawn(mView)
            }
        }
    }

    class SavedState : View.BaseSavedState {

        @State
        internal val state: Int

        constructor(source: Parcel) : super(source) {
            state = source.readInt()
        }

        constructor(superState: Parcelable, @State state: Int) : super(superState) {
            this.state = state
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(state)
        }

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {

        /**
         * The bottom sheet is dragging.
         */
        const val STATE_DRAGGING = 1

        /**
         * The bottom sheet is settling.
         */
        const val STATE_SETTLING = 2

        /**
         * The bottom sheet is expanded_half_way.
         */
        const val STATE_ANCHOR_POINT = 3

        /**
         * The bottom sheet is expanded.
         */
        const val STATE_EXPANDED = 4

        /**
         * The bottom sheet is collapsed.
         */
        const val STATE_COLLAPSED = 5

        /**
         * The bottom sheet is hidden.
         */
        const val STATE_HIDDEN = 6

        /**
         * A utility function to get the [KaleyraBottomSheetBehaviour] associated with the `view`.
         *
         * @param view The [View] with [KaleyraBottomSheetBehaviour].
         * @param <V>  Instance of behavior
         * @return The [KaleyraBottomSheetBehaviour] associated with the `view`.
         */
        @Suppress("UNCHECKED_CAST")
        fun <V : View> from(view: V): KaleyraBottomSheetBehaviour<V> {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
                ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            return params.behavior as? KaleyraBottomSheetBehaviour<V>
                ?: throw IllegalArgumentException("The view is not associated with KaleyraBottomSheetBehaviour")
        }

        private const val HIDE_THRESHOLD = 0.5f

        private const val HIDE_FRICTION = 0.1f

        private const val MIN_VELOCITY_SKIP_ANCHOR_STATE = 4000
        private const val MAX_VELOCITY_SKIP_ANCHOR_STATE = 10000

        private const val MIN_VELOCITY = 300
    }

}