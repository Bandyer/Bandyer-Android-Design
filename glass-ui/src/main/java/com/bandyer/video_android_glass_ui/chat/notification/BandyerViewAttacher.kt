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
    /**
     * The view to which attach the view
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

    init {
        view.apply {
            id = View.generateViewId()
            visibility = View.GONE
        }
    }

    override fun attach() {
        if(layout.findViewById<View>(view.id) != null) return

        val constraintSet = ConstraintSet()
        layout.addView(view)

        constraintSet.clone(layout)
        constraintSet.connect(
            view.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            view.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.connect(
            view.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        constraintSet.constrainWidth(view.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.applyTo(layout)
    }
}

/**
 * BandyerFrameLayoutAttacher
 */
internal class BandyerFrameLayoutAttacher(
    override val layout: FrameLayout,
    override val view: View
) : BandyerViewAttacher {

    init {
        view.apply {
            id = View.generateViewId()
            visibility = View.GONE
        }
    }

    /**
     * Attach the [view] to a [FrameLayout]
     */
    override fun attach() {
        if(layout.findViewById<View>(view.id) != null) return

        layout.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
            )
        )
    }
}

/**
 * BandyerRelativeLayoutAttacher
 */
internal class BandyerRelativeLayoutAttacher(
    override val layout: RelativeLayout,
    override val view: View
) : BandyerViewAttacher {

    init {
        view.apply {
            id = View.generateViewId()
            visibility = View.GONE
        }
    }

    override fun attach() {
        if(layout.findViewById<View>(view.id) != null) return

        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        layout.addView(view, params)
    }
}