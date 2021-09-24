package com.bandyer.video_android_glass_ui.chat.notification

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

/**
 * A ViewAttacher is used to attach a view to a layout
 *
 * @property layout ViewGroup
 * @property view View
 */
internal interface BandyerViewAttacher {

    companion object Factory {
        /**
         * Create an instance of a [BandyerViewAttacher]
         *
         * @param layout The layout to which attach the view
         * @param view The view to attach
         * @return BandyerViewAttacher
         */
        fun create(layout: ViewGroup, view: View): BandyerViewAttacher {
            view.apply {
                id = View.generateViewId()
                visibility = View.GONE
            }
            return when (layout) {
                is ConstraintLayout -> BandyerConstraintLayoutAttacher(layout, view)
                is FrameLayout      -> BandyerFrameLayoutAttacher(layout, view)
                is RelativeLayout   -> BandyerRelativeLayoutAttacher(layout, view)
                else                -> throw IllegalArgumentException("Unsupported layout type")
            }
        }
    }

    /**
     * The view group to which attach the view
     */
    val layout: ViewGroup

    /**
     * The view to attach
     */
    val view: View

    /**
     * Attach the [view] to the [layout]
     */
    fun attach()

    /**
     *  Detach the [view] from the previously passed layout
     */
    fun detach() = layout.removeView(view)
}

/**
 * BandyerConstraintLayoutAttacher
 */
internal class BandyerConstraintLayoutAttacher(
    override val layout: ConstraintLayout,
    override val view: View
) : BandyerViewAttacher {

    override fun attach() {
        if (layout.findViewById<View>(view.id) != null) return
        layout.addView(view)

        ConstraintSet().apply {
            clone(layout)
            connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constrainWidth(view.id, ConstraintSet.MATCH_CONSTRAINT)
            applyTo(layout)
        }
    }
}

/**
 * BandyerFrameLayoutAttacher
 */
private class BandyerFrameLayoutAttacher(
    override val layout: FrameLayout,
    override val view: View
) : BandyerViewAttacher {

    /**
     * Attach the [view] to a [FrameLayout]
     */
    override fun attach() {
        if (layout.findViewById<View>(view.id) != null) return
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        )
        layout.addView(view, params)
    }
}

/**
 * BandyerRelativeLayoutAttacher
 */
private class BandyerRelativeLayoutAttacher(
    override val layout: RelativeLayout,
    override val view: View
) : BandyerViewAttacher {

    override fun attach() {
        if (layout.findViewById<View>(view.id) != null) return
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply { addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE) }
        layout.addView(view, params)
    }
}