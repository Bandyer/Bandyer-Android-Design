/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.video_android_phone_ui.extensions

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.*
import android.hardware.input.InputManager
import android.os.Build
import android.os.SystemClock
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.InputDeviceCompat
import com.bandyer.android_common.FieldProperty
import com.bandyer.android_common.LifecycleEvents
import com.bandyer.video_android_phone_ui.buttons.BandyerActionButton
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


////////////////////////////////////////////// FIELD PROPERTIES //////////////////////////////////////////////

/**
 * Describes if the view is floating inside a coordinator layout.
 */
var View.isFloating: Boolean by FieldProperty { false }

/**
 * Describes if this view has a double tap listener.
 */
var View.hasDoubleTapListener: Boolean by FieldProperty { false }

/**
 * Describes current clamping corner if the view is currently floating.
 */
var View.currentCorner: CORNER? by FieldProperty { CORNER.BOTTOM_RIGHT }

/**
 * A pulse animation used to highlight a selected view.
 */
var View.pulseAnimation: ObjectAnimator? by FieldProperty { null }

internal var View.isFlinging: Boolean by FieldProperty { false }
internal var View.isUnHooked: Boolean by FieldProperty { false }

////////////////////////////////////////////////// EXTENSION FUNCTIONS ////////////////////////////////////////////////////////////////////////////////////////



/**
 * This utils function make a view change its bounds as a square, its dimensions to a desired size and moves the view to a desired top
 * and a desired left.
 * @receiver View the moving view
 * @param toSize desired square side size
 * @param toTop desired top position
 * @param toLeft desired left position
 * @param toRight desired right position
 * @param onResizedAndMoved () -> Unit? callback after animator has ended
 */
fun View.resizeAndMove(toSize: Float, toTop: Float, toLeft: Float, toRight: Float, duration: Long, onResizedAndMoved: () -> Unit): AnimatorSet? {
    val leftMargin = if (!context.isRtl()) toLeft else toRight
    val rightMargin = if (!context.isRtl()) toRight else toLeft

    val valueAnimator = ValueAnimator.ofFloat(layoutParams.height.toFloat().takeIf { it != -1f } ?: height.toFloat(), toSize)
    val valueAnimator3 = ValueAnimator.ofFloat((layoutParams as ViewGroup.MarginLayoutParams).topMargin.toFloat(), toTop)
    val valueAnimator4 = ValueAnimator.ofFloat(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        (layoutParams as ViewGroup.MarginLayoutParams).marginStart.toFloat()
    } else {
        (layoutParams as ViewGroup.MarginLayoutParams).leftMargin.toFloat()
    }, leftMargin)
    val valueAnimator5 = ValueAnimator.ofFloat(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        (layoutParams as ViewGroup.MarginLayoutParams).marginEnd.toFloat()
    } else {
        (layoutParams as ViewGroup.MarginLayoutParams).rightMargin.toFloat()
    }, rightMargin)

    valueAnimator.addUpdateListener {
        val value = it.animatedValue as Float
        layoutParams.height = value.toInt()
        requestLayout()
    }

    valueAnimator3.addUpdateListener {
        val value = it.animatedValue as Float
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        lp.topMargin = value.toInt()
        requestLayout()
    }

    valueAnimator4.addUpdateListener {
        val value = it.animatedValue as Float
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) lp.marginStart = value.toInt()
        else lp.leftMargin = value.toInt()
        requestLayout()
    }

    valueAnimator5.addUpdateListener {
        val value = it.animatedValue as Float
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) lp.marginEnd = value.toInt()
        else lp.rightMargin = value.toInt()
        requestLayout()
    }

    val set = AnimatorSet()
    set.playTogether(valueAnimator, valueAnimator3, valueAnimator4, valueAnimator5)
    set.interpolator = AccelerateDecelerateInterpolator()
    set.duration = duration
    set.addListener(object : Animator.AnimatorListener {
        private var isCanceled = false
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {
            if (isCanceled) return
            invalidate()
            requestLayout()
            onResizedAndMoved.invoke()
        }

        override fun onAnimationCancel(animation: Animator?) {
            isCanceled = true
        }
        override fun onAnimationStart(animation: Animator?) {}
    })
    set.start()
    return set
}

/**
 * This function makes a view draggable and flingable inside a coordinator layout.
 * The view is anchored on the nearest container corner.
 * @receiver View
 * @param container CoordinatorLayout
 * @param topAnchor View
 * @param bottomAnchor View
 */
@SuppressLint("ClickableViewAccessibility")
fun View.makeFloating(container: androidx.coordinatorlayout.widget.CoordinatorLayout,
                      topAnchor: View, bottomAnchor: View,
                      initialCorner: CORNER,
                      onSnapped: FloatingViewCornerSnappedListener? = null,
                      onFloatingViewClickListener: View.OnClickListener? = null,
                      onFloatingViewDoubleClickListener: OnDoubleClickListener? = null
) {

    if (isFloating) return
    isFloating = true

    (this.context as? AppCompatActivity)?.let {
        com.bandyer.android_common.LifecyleBinder.bind(it, object : LifecycleEvents {
            override fun create() {}
            override fun destroy() {
                this@makeFloating.clearAnimation()
            }

            override fun pause() {}
            override fun resume() {}
            override fun start() {}
            override fun stop() {}
        })
    }

    var downPointEvent: Point? = null
    var movePointEvent: Point? = null

    val gestureDetector = GestureDetector(this.context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            onFloatingViewClickListener?.onClick(this@makeFloating)
            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            onFloatingViewDoubleClickListener?.onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onFling(downEvent: MotionEvent, moveEvent: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

            if (!isFloating) return true

            if (isFlinging) return true

            val normalizedTranslationX = abs(translationX)
            val normalizedTranslationY = abs(translationY)

            if (normalizedTranslationX == 0f && normalizedTranslationY == 0f) return true

            val isDiagonalAngle = isDiagonalAngle(
                    downPointEvent!!,
                    Point(moveEvent.rawX.toInt(), moveEvent.rawY.toInt())
            )

            if (downPointEvent!!.x == movePointEvent!!.x && downPointEvent!!.y == movePointEvent!!.y) return true

            isFlinging = true

            when (currentCorner) {
                CORNER.TOP_LEFT -> when {
                    isDiagonalAngle -> animateToCorner(CORNER.BOTTOM_RIGHT, container, topAnchor, bottomAnchor, false, onSnapped)
                    normalizedTranslationX > normalizedTranslationY -> animateToCorner(CORNER.TOP_RIGHT, container, topAnchor, bottomAnchor, false, onSnapped)
                    else -> animateToCorner(CORNER.BOTTOM_LEFT, container, topAnchor, bottomAnchor, false, onSnapped)
                }

                CORNER.TOP_RIGHT -> when {
                    isDiagonalAngle -> animateToCorner(CORNER.BOTTOM_LEFT, container, topAnchor, bottomAnchor, false, onSnapped)
                    normalizedTranslationX > normalizedTranslationY -> animateToCorner(CORNER.TOP_LEFT, container, topAnchor, bottomAnchor, false, onSnapped)
                    else -> animateToCorner(CORNER.BOTTOM_RIGHT, container, topAnchor, bottomAnchor, false, onSnapped)
                }

                CORNER.BOTTOM_LEFT -> when {
                    isDiagonalAngle -> animateToCorner(CORNER.TOP_RIGHT, container, topAnchor, bottomAnchor, false, onSnapped)
                    normalizedTranslationX > normalizedTranslationY -> animateToCorner(CORNER.BOTTOM_RIGHT, container, topAnchor, bottomAnchor, false, onSnapped)
                    else -> animateToCorner(CORNER.TOP_LEFT, container, topAnchor, bottomAnchor, false, onSnapped)
                }

                CORNER.BOTTOM_RIGHT -> when {
                    isDiagonalAngle -> animateToCorner(CORNER.TOP_LEFT, container, topAnchor, bottomAnchor, false, onSnapped)
                    normalizedTranslationX > normalizedTranslationY -> animateToCorner(CORNER.BOTTOM_LEFT, container, topAnchor, bottomAnchor, false, onSnapped)
                    else -> animateToCorner(CORNER.TOP_RIGHT, container, topAnchor, bottomAnchor, false, onSnapped)
                }
            }
            return true
        }
    })

    this.setOnTouchListener(object : View.OnTouchListener {

        private var mXDiffInTouchPointAndViewTopLeftCorner: Float = 0f
        private var mYDiffInTouchPointAndViewTopLeftCorner: Float = 0f

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (!isFloating) return true

            container.requestDisallowInterceptTouchEvent(true)

            gestureDetector.onTouchEvent(event)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downPointEvent = Point(event.rawX.toInt(), event.rawY.toInt())
                    movePointEvent = Point(event.rawX.toInt(), event.rawY.toInt())
                    // calculate the difference between touch point(event.getRawX()) on view & view's top left corner(v.getX())
                    mXDiffInTouchPointAndViewTopLeftCorner = event.rawX - v.x
                    mYDiffInTouchPointAndViewTopLeftCorner = event.rawY - v.y
                }
                MotionEvent.ACTION_MOVE -> {

                    when {
                        (currentCorner == CORNER.TOP_LEFT || currentCorner == CORNER.TOP_RIGHT) -> {
                            if (event.rawY < movePointEvent!!.y
                                    && event.rawY > topAnchor.bottom
                            )
                                downPointEvent = Point(event.rawX.toInt(), event.rawY.toInt())
                        }
                        (currentCorner == CORNER.BOTTOM_LEFT || currentCorner == CORNER.BOTTOM_RIGHT) -> {
                            if (event.rawY > movePointEvent!!.y
                                    && event.rawY < context.getScreenSize().y - getBottomAnchorOffset(bottomAnchor)
                            )
                                downPointEvent = Point(event.rawX.toInt(), event.rawY.toInt())
                        }
                    }

                    movePointEvent = Point(event.rawX.toInt(), event.rawY.toInt())

                    val newTopLeftX = event.rawX - mXDiffInTouchPointAndViewTopLeftCorner
                    val newTopLeftY = event.rawY - mYDiffInTouchPointAndViewTopLeftCorner

                    val targetRight = newTopLeftX + v.width + (v.layoutParams as ViewGroup.MarginLayoutParams).rightMargin
                    val newX = when {
                        targetRight < container.width && newTopLeftX > (v.layoutParams as ViewGroup.MarginLayoutParams).leftMargin -> newTopLeftX
                        targetRight > container.width -> container.width.toFloat() - v.width - (v.layoutParams as ViewGroup.MarginLayoutParams).rightMargin
                        newTopLeftX < (v.layoutParams as ViewGroup.MarginLayoutParams).leftMargin -> (v.layoutParams as ViewGroup.MarginLayoutParams).leftMargin.toFloat()
                        else -> newTopLeftX
                    }

                    val targetBottom = newTopLeftY + v.height + (v.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
                    val currentTopAnchorBottom = topAnchor.bottom + topAnchor.translationY
                    val newY = when {
                        targetBottom < bottomAnchor.top && newTopLeftY > currentTopAnchorBottom -> newTopLeftY
                        targetBottom > bottomAnchor.top -> bottomAnchor.bottom.toFloat() - v.height - (v.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
                        newTopLeftY < currentTopAnchorBottom -> currentTopAnchorBottom
                        else -> newTopLeftY
                    }

                    v.x = newX
                    v.y = newY

                    val cornerPoint = Point(0, 0)
                    val nearbyPoint = Point(newX.toInt() + v.width / 2, newY.toInt() + v.height / 2)

                    val nearbyCorner = getNearestCornerForView(v, container, bottomAnchor)
                    when (nearbyCorner) {
                        CORNER.TOP_LEFT -> {
                            cornerPoint.x = 0
                            cornerPoint.y = topAnchor.bottom
                        }
                        CORNER.TOP_RIGHT -> {
                            cornerPoint.x = rootView.context.getScreenSize().x
                            cornerPoint.y = topAnchor.bottom
                        }
                        CORNER.BOTTOM_LEFT -> {
                            cornerPoint.x = 0
                            cornerPoint.y = bottomAnchor.top
                        }
                        CORNER.BOTTOM_RIGHT -> {
                            cornerPoint.x = rootView.context.getScreenSize().x
                            cornerPoint.y = bottomAnchor.top
                        }
                    }
                    val unHookedDx = cornerPoint.x - nearbyPoint.x
                    val unHookedDy = cornerPoint.y - nearbyPoint.y
                    val unHookedDistance = sqrt(((unHookedDx.toLong() * unHookedDx.toLong()) + (unHookedDy.toLong() * unHookedDy.toLong())).toDouble())

                    if (unHookedDistance > v.height * 1.5 && !isUnHooked) {
                        isUnHooked = true
                        onSnapped?.onUnHooked(currentCorner!!)
                    } else if (isUnHooked) {
                        if (unHookedDistance < v.height) {
                            isUnHooked = false
                            onSnapped?.onSnap(nearbyCorner)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!isFlinging) {
                        isFlinging = true
                        animateToCorner(getNearestCornerForView(v, container, bottomAnchor), container, topAnchor, bottomAnchor, false, onSnapped)
                    }
                }
            }
            return true
        }
    })

    setOnClickListener(onFloatingViewClickListener)

    bringToFront()

    animateToCorner(initialCorner, container, topAnchor, bottomAnchor, true, onSnapped)
}

/**
 * @suppress
 */
internal fun View.getBottomAnchorOffset(bottomAnchor: View): Float {
    return (context.getScreenSize().y - bottomAnchor.top).toFloat()
}

/**
 * @suppress
 */
fun View.animateToCorner(corner: CORNER, container: androidx.coordinatorlayout.widget.CoordinatorLayout, topAnchor: View, bottomAnchor: View, instant: Boolean = false, onSnapped: FloatingViewCornerSnappedListener? = null) {
    if (!isFloating) return

    isFlinging = true

    if (this.animation != null)
        this.animation.cancel()
    this.clearAnimation()

    var animatorTranslationX = 0f
    var animatorTranslationY = 0f

    val marginTop = (this.layoutParams as ViewGroup.MarginLayoutParams).topMargin
    val marginBottom = (this.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
    val marginLeft = (this.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
    val marginRight = (this.layoutParams as ViewGroup.MarginLayoutParams).rightMargin
    val topAnchorOffset = topAnchor.height + topAnchor.translationY
    val statusBarMargin = context.dp2px(16f)

    when (currentCorner) {

        CORNER.TOP_LEFT -> when (corner) {
            CORNER.TOP_LEFT -> {
            }
            CORNER.TOP_RIGHT -> {
                animatorTranslationX = context.getScreenSize().x - width.toFloat() - marginRight - marginLeft
            }
            CORNER.BOTTOM_LEFT -> {
                animatorTranslationY = context.getScreenSize().y - height.toFloat() - topAnchorOffset - getBottomAnchorOffset(bottomAnchor) - marginBottom - marginTop - statusBarMargin
            }
            CORNER.BOTTOM_RIGHT -> {
                animatorTranslationX = context.getScreenSize().x - width.toFloat() - marginLeft - marginRight
                animatorTranslationY = context.getScreenSize().y - height.toFloat() - topAnchorOffset - getBottomAnchorOffset(bottomAnchor) - marginBottom - marginTop - statusBarMargin
            }
        }

        CORNER.TOP_RIGHT -> when (corner) {
            CORNER.TOP_LEFT -> {
                animatorTranslationX = -(context.getScreenSize().x - width.toFloat()) + marginLeft + marginRight
            }
            CORNER.TOP_RIGHT -> {
            }
            CORNER.BOTTOM_LEFT -> {
                animatorTranslationX = -(context.getScreenSize().x - width.toFloat()) + marginLeft + marginRight
                animatorTranslationY = context.getScreenSize().y - height.toFloat() - topAnchorOffset - getBottomAnchorOffset(bottomAnchor) - marginBottom - marginTop - statusBarMargin
            }
            CORNER.BOTTOM_RIGHT -> {
                animatorTranslationY = context.getScreenSize().y - height.toFloat() - topAnchorOffset - getBottomAnchorOffset(bottomAnchor) - marginBottom - marginTop - statusBarMargin
            }
        }

        CORNER.BOTTOM_LEFT -> when (corner) {
            CORNER.TOP_LEFT -> {
                animatorTranslationY = -(context.getScreenSize().y - height.toFloat() - topAnchorOffset - getBottomAnchorOffset(bottomAnchor)) + marginTop + marginBottom + statusBarMargin
            }
            CORNER.TOP_RIGHT -> {
                animatorTranslationX = context.getScreenSize().x - width.toFloat() - marginRight - marginLeft
                animatorTranslationY = -(context.getScreenSize().y - height.toFloat() - topAnchorOffset - getBottomAnchorOffset(bottomAnchor)) + marginTop + marginBottom + statusBarMargin
            }
            CORNER.BOTTOM_LEFT -> {
            }
            CORNER.BOTTOM_RIGHT -> {
                animatorTranslationX = context.getScreenSize().x - width.toFloat() - marginRight - marginLeft
            }
        }

        CORNER.BOTTOM_RIGHT -> when (corner) {
            CORNER.TOP_LEFT -> {
                animatorTranslationX = -(context.getScreenSize().x - width.toFloat()) + marginLeft + marginRight
                animatorTranslationY = -(context.getScreenSize().y - height.toFloat() - topAnchorOffset - getBottomAnchorOffset(bottomAnchor)) + marginTop + marginBottom + statusBarMargin
            }
            CORNER.TOP_RIGHT -> {
                animatorTranslationY = -(context.getScreenSize().y - height.toFloat() - topAnchorOffset - getBottomAnchorOffset(bottomAnchor)) + marginTop + marginBottom + statusBarMargin
            }
            CORNER.BOTTOM_LEFT -> {
                animatorTranslationX = -(context.getScreenSize().x - width.toFloat()) + marginLeft + marginRight
            }
            CORNER.BOTTOM_RIGHT -> {
            }
        }
    }

    val animatorX = ObjectAnimator.ofFloat(this, "translationX", this.translationX, animatorTranslationX)
    animatorX.interpolator = DecelerateInterpolator(0.90f)

    val animatorY = ObjectAnimator.ofFloat(this, "translationY", this.translationY, animatorTranslationY)
    animatorY.interpolator = AccelerateInterpolator(0.90f)

    AnimatorSet().apply {
        this.duration = if (instant) 0 else 350
        this.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            @SuppressLint("RtlHardcoded")
            override fun onAnimationEnd(animation: Animator?) {
                if (!isFloating) return

                isFlinging = false
                translationY = 0f
                translationX = 0f

                val lp = (layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams)

                when (corner) {

                    CORNER.TOP_LEFT -> {
                        lp.anchorId = topAnchor.id
                        lp.anchorGravity = Gravity.BOTTOM or Gravity.LEFT
                        lp.gravity = Gravity.BOTTOM or Gravity.LEFT
                    }

                    CORNER.TOP_RIGHT -> {
                        lp.anchorId = topAnchor.id
                        lp.anchorGravity = Gravity.BOTTOM or Gravity.RIGHT
                        lp.gravity = Gravity.BOTTOM or Gravity.RIGHT
                    }

                    CORNER.BOTTOM_LEFT -> {
                        lp.anchorId = bottomAnchor.id
                        lp.anchorGravity = Gravity.TOP or Gravity.LEFT
                        lp.gravity = Gravity.TOP or Gravity.LEFT
                    }

                    CORNER.BOTTOM_RIGHT -> {
                        lp.anchorId = bottomAnchor.id
                        lp.anchorGravity = Gravity.TOP or Gravity.RIGHT
                        lp.gravity = Gravity.TOP or Gravity.RIGHT
                    }
                }

                requestLayout()

                currentCorner = corner

                container.requestDisallowInterceptTouchEvent(false)

                isUnHooked = false

                onSnapped?.onSnap(currentCorner!!)
            }

            override fun onAnimationCancel(animation: Animator?) {
                container.requestDisallowInterceptTouchEvent(false)
            }

            override fun onAnimationStart(animation: Animator?) {}
        })
        this.playTogether(animatorX, animatorY)
        this.start()
    }
}

/**
 * @suppress
 */
internal fun View.getNearestCornerForView(view: View, container: ViewGroup, bottomAnchor: View): CORNER {

    val location = view.getLocationInContainer(container)

    val top = location.y
    val left = location.x

    val isLeft = left < container.width / 2
    val isTop = top < (container.height - getBottomAnchorOffset(bottomAnchor)) / 2

    return when {
        isLeft && isTop -> CORNER.TOP_LEFT
        isLeft && !isTop -> CORNER.BOTTOM_LEFT
        !isLeft && isTop -> CORNER.TOP_RIGHT
        !isLeft && !isTop -> CORNER.BOTTOM_RIGHT
        else -> CORNER.TOP_LEFT
    }
}

/**
 * @suppress
 */
internal fun View.isDiagonalAngle(one: Point, two: Point): Boolean {

    when (currentCorner) {
        CORNER.TOP_LEFT -> {
            if (two.x < one.x || two.y < one.y) return false
        }
        CORNER.TOP_RIGHT -> {
            if (two.x > one.x || two.y < one.y) return false
        }
        CORNER.BOTTOM_LEFT -> {
            if (two.x < one.x || two.y > one.y) return false
        }
        CORNER.BOTTOM_RIGHT -> {
            if (two.x > one.x || two.y > one.y) return false
        }
    }

    val angle = atan2(one.y.toDouble() - two.y.toDouble(), one.x.toDouble() - two.x.toDouble())
    val normalizedAngle = abs((angle * 180 / 3.14) % 90)
    return normalizedAngle > 20 && normalizedAngle < 70
}

/**
 * This class represents screen corners.
 */
enum class CORNER {

    /**
     * top left
     */
    TOP_LEFT,

    /**
     * top right
     */
    TOP_RIGHT,

    /**
     * bottom left
     */
    BOTTOM_LEFT,

    /**
     * bottom right
     */
    BOTTOM_RIGHT
}


/**
 * This class represents a listener that will be notified when a double click occurs on some view.
 */
interface OnDoubleClickListener {
    /**
     * Called when the view is double clicked.
     */
    fun onDoubleClick()
}

/**
 * This class represents a floating view listener that is notified when the view is snapped on a corner
 * or just went off from a corner.
 */
interface FloatingViewCornerSnappedListener {

    /**
     * When a view gets anchored to a corner
     * @param corner CORNER
     */
    fun onSnap(corner: CORNER)

    /**
     * When a view gets unhooked from a corner
     * @param fromCorner CORNER
     */
    fun onUnHooked(fromCorner: CORNER)
}

/**
 * Returns in screen coordinates as Point of a view
 * @receiver view View
 * @return Point
 */
fun View.getCoordinates(): Point {
    val location = IntArray(2)
    getLocationInWindow(location)
    return Point(location[0], location[1])
}

/**
 * Retrieves alpha value even for api level less than 19
 * @return alpha level
 */
internal var Drawable.alphaCompat: Int? by FieldProperty { DrawableCompat.getAlpha(it) }

/**
 * Method to retrieve font using the compat library
 * @receiver View requesting
 * @param fontFamily font family to apply
 * @param style Typeface.NORMAL
 * @return Typeface retrieved
 */
@SuppressLint("RestrictedApi")
fun View.getFont(fontFamily: Int, style: Int): Typeface {
    return ResourcesCompat.getFont(context, fontFamily, TypedValue(), style, null)
}

/**
 * Hide keyboard
 * @receiver View
 * @param forced true if should be hidden no matter what, false otherwise
 */
fun View.hideKeyboard(forced: Boolean = false) {
    val inputManager = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val hideMethod = if (forced) 0 else InputMethodManager.HIDE_NOT_ALWAYS
    inputManager.hideSoftInputFromWindow(this.windowToken, hideMethod)
}

/**
 * Show keyboard
 * @receiver View
 * @param forced true if should be shown no matter what, false otherwise
 */
fun View.showKeyboard(forced: Boolean = false) {
    val inputManager = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val showMethod = if (forced) InputMethodManager.SHOW_FORCED else InputMethodManager.SHOW_IMPLICIT
    inputManager.showSoftInput(this, showMethod)
}


/**
 * Animates view with pulse effect repeating infinite times.
 * @receiver View
 */
fun View.pulse() {
    pulseAnimation = ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f))
    pulseAnimation?.duration = 750
    pulseAnimation?.repeatCount = ObjectAnimator.INFINITE
    pulseAnimation?.repeatMode = ObjectAnimator.REVERSE
    pulseAnimation?.start()
}

/**
 * Cancel pulse animation.
 * @receiver View
 */
fun View.cancelPulse() {
    pulseAnimation?.cancel()
    scaleX = 1.0f
    scaleY = 1.0f
}


/**
 * Performs a double tap programmatic gesture on this view.
 * @receiver View
 */
fun View.performDoubleTap() {
    if (!hasDoubleTapListener) return
    performTap()
    postDelayed({ performTap() }, ViewConfiguration.getDoubleTapTimeout() / 2.5.toLong())
}

/**
 * Performs a tap programmatic gesture on this view.
 * @receiver View
 */
fun View.performTap(): Boolean {
    val rect = Rect()
    this.getLocalVisibleRect(rect)
    val tapX = rect.left + this.width / 2f
    val tapY = rect.top + this.height / 2f

    val downTime = SystemClock.uptimeMillis()
    val eventTime = downTime + 200
    val properties = arrayOfNulls<MotionEvent.PointerProperties>(1)
    val pp1 = MotionEvent.PointerProperties()
    pp1.id = 0
    pp1.toolType = MotionEvent.TOOL_TYPE_FINGER
    properties[0] = pp1
    val pointerCoords = arrayOfNulls<MotionEvent.PointerCoords>(1)
    val pc1 = MotionEvent.PointerCoords()
    pc1.x = tapX
    pc1.y = tapY
    pc1.pressure = 1f
    pc1.size = 1f
    pointerCoords[0] = pc1

    var motionEvent = MotionEvent.obtain(downTime, eventTime,
            MotionEvent.ACTION_DOWN, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_DPAD, 0)

    dispatchTouchEvent(motionEvent)
    motionEvent.recycle()
    motionEvent = MotionEvent.obtain(motionEvent)
    motionEvent.action = MotionEvent.ACTION_UP
    dispatchTouchEvent(motionEvent)
    motionEvent.recycle()
    return true
}

/**
 *
 * Creates a touch event
 * @receiver View
 * @param x Float
 * @param y Float
 */
@SuppressLint("PrivateApi")
fun View.createTouch(x: Float, y: Float) {
    var methodName = "getInstance"
    val objArr = arrayOfNulls<Any>(0)
    var im: InputManager? = null
    try {
        im = InputManager::class.java.getDeclaredMethod(methodName, *arrayOfNulls(0))
                .invoke(null, *objArr) as InputManager
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }
    //Make MotionEvent.obtain() method accessible
    methodName = "obtain"
    try {
        MotionEvent::class.java.getDeclaredMethod(methodName, *arrayOfNulls(0)).isAccessible = true
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }
    //Get the reference to injectInputEvent method
    methodName = "injectInputEvent"
    var injectInputEventMethod: Method? = null
    try {
        injectInputEventMethod = InputManager::class.java
                .getMethod(methodName, *arrayOf(InputEvent::class.java, Integer.TYPE))
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }
    val `when` = SystemClock.uptimeMillis()
    val source = InputDeviceCompat.SOURCE_TOUCHSCREEN
    val pressure = 1.0f
    val action = MotionEvent.ACTION_CANCEL
    @SuppressLint("Recycle") val event = MotionEvent.obtain(`when`, `when`, action, x, y, pressure,
            1.0f, 0, 1.0f, 1.0f, 0, 0)
    event.source = source
    try {
        assert(injectInputEventMethod != null)
        injectInputEventMethod!!.invoke(im, event, 0)
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    }
}

/**
 * Performs click recursively in all view's child.
 * @receiver View
 */
fun View.performNestedClick(): Boolean {
    if (isClickable || hasDoubleTapListener) return performClickAndCancelPulseAnimation()
    when (this) {
        is ViewGroup ->
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                return child.performNestedClick()
            }
    }
    return false
}

/**
 * Let focusable views be also focusable in touch mode for single finger navigation gestures on google glass
 * @receiver View
 */
fun View.setAllViewsFocusableInTouchMode() {
    when {
        this is ViewGroup && !isFocusable -> {
            (0 until childCount).forEach {
                val child = getChildAt(it)
                child.setAllViewsFocusableInTouchMode()
            }
        }
        else -> if (isFocusable || this is BandyerActionButton) {
            isFocusableInTouchMode = true
        }
    }
}


/**
 * Performs click and cancel pulse animation.
 * @receiver View
 */
fun View.performClickAndCancelPulseAnimation(): Boolean {
    cancelPulse()
    return performTap()
}

/**
 * Performs double tap recursively in all view's child.
 * @receiver View
 */
fun View.performNestedDoubleClick() {
    performDoubleTapAndCancelPulseAnimation()
    when (this) {
        is ViewGroup ->
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is ViewGroup) child.performNestedDoubleClick()
                else child.performDoubleTapAndCancelPulseAnimation()
            }
    }
}

/**
 * Performs click and cancel pulse animation.
 * @receiver View
 */
fun View.performDoubleTapAndCancelPulseAnimation() {
    performDoubleTap()
    cancelPulse()
}

/**
 * Returns location in container as pair of coordinates of the view's center.
 * @receiver View
 * @param container ViewGroup
 * @return Pair<Float, Float>
 */
fun View.getLocationInContainer(container: ViewGroup): PointF {
    val viewLocation = IntArray(2)
    this.getLocationInWindow(viewLocation)
    val rootLocation = IntArray(2)
    container.getLocationInWindow(rootLocation)
    val relativeLeft = viewLocation[0] - rootLocation[0]
    val relativeTop = viewLocation[1] - rootLocation[1]
    return PointF(
            relativeLeft.toFloat() + (layoutParams as ViewGroup.MarginLayoutParams).leftMargin + width / 2,
            relativeTop.toFloat() + (layoutParams as ViewGroup.MarginLayoutParams).topMargin + height / 2)
}

/**
 * Retrieves index of specified view id inside this view group or -1 if the specified child is not a view group child
 * @receiver ViewGroup
 * @param viewId Int the view id to look for in the view group
 * @return Int the index of the view in view group
 */
fun ViewGroup.getViewIndex(viewId: Int?): Int {
    viewId ?: return -1
    (0..childCount).forEach { index ->
        if (getChildAt(index)?.id == viewId)
            return index
    }
    return -1
}

/**
 * Enable/Disable all views
 * @receiver View
 * @param enabled Boolean to enable or disable a view
 */
fun View.setAllEnabled(enabled: Boolean) {
    isEnabled = enabled
    if (this !is ViewGroup) return
    for (i in 0 until childCount) getChildAt(i).setAllEnabled(enabled)
}

/**
 * Set the bottom padding for a view
 * @receiver View
 * @param px The padding to be set expressed in pixel
 */
fun View.setPaddingBottom(@Px px: Int) {
    this.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, px)
}

/**
 * Set the end padding for a view
 * @receiver View
 * @param px The padding to be set expressed in pixel
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
fun View.setPaddingEnd(@Px px: Int) {
    this.setPaddingRelative(this.paddingStart, this.paddingTop, px, this.paddingBottom)
}

/**
 * Set the horizontal padding for a view
 * @receiver View
 * @param px The padding to be set expressed in pixel
 */
fun View.setPaddingHorizontal(@Px px: Int) {
    this.setPadding(px, this.paddingTop, px, this.paddingBottom)
}

/**
 * Set the left padding for a view
 * @receiver View
 * @param px The padding to be set expressed in pixel
 */
fun View.setPaddingLeft(@Px px: Int) {
    this.setPadding(px, this.paddingTop, this.paddingRight, this.paddingBottom)
}

/**
 * Set the right padding for a view
 * @receiver View
 * @param px The padding to be set expressed in pixel
 */
fun View.setPaddingRight(@Px px: Int) {
    this.setPadding(this.paddingLeft, this.paddingTop, px, this.paddingBottom)
}

/**
 * Set the start padding for a view
 * @receiver View
 * @param px The padding to be set expressed in pixel
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
fun View.setPaddingStart(@Px px: Int) {
    this.setPaddingRelative(px, this.paddingTop, this.paddingEnd, this.paddingBottom)
}

/**
 * Set the top padding for a view
 * @receiver View
 * @param px The padding to be set expressed in pixel
 */
fun View.setPaddingTop(@Px px: Int) {
    this.setPadding(this.paddingLeft, px, this.paddingRight, this.paddingBottom)
}

/**
 * Set the vertical padding for a view
 * @receiver View
 * @param px The padding to be set expressed in pixel
 */
fun View.setPaddingVertical(@Px px: Int) {
    this.setPadding(this.paddingLeft, px, this.paddingRight, px)
}

/**
 * Get the sum of the view height and its vertical margins
 * @receiver View
 * @return Int the height and the vertical margins
 */
fun View.getHeightWithVerticalMargin(): Int = height + (this.layoutParams as ViewGroup.MarginLayoutParams).topMargin + (this.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

/**
 * Replace the view with the specified one
 * @receiver View
 * @param newView The view that replace the target one
 */
fun View.replaceWith(newView: View) {
    val thisParent = parent as? ViewGroup ?: return
    val otherParent = newView.parent as? ViewGroup
    newView.id = id
    newView.layoutParams = layoutParams
    val index = thisParent.indexOfChild(this)
    thisParent.removeView(this)
    otherParent?.removeView(newView)
    thisParent.addView(newView, index)
}