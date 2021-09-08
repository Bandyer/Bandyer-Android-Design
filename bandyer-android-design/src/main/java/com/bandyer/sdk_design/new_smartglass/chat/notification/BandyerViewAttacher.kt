package com.bandyer.sdk_design.new_smartglass.chat.notification

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

/**
 * Class used to attach a view to a given layout
 *
 * @property view The view to be attached
 * @constructor
 */
class BandyerViewAttacher(val view: View) {

    private var parentView: ViewGroup? = null

    init {
        view.apply {
            id = View.generateViewId()
            visibility = View.GONE
        }
    }

    /**
     * Attach the [view] to a [ConstraintLayout]
     *
     * @param layout The layout on which attach the [view]
     */
    fun attach(layout: ConstraintLayout) {
        parentView = layout

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

    /**
     * Attach the [view] to a [FrameLayout]
     *
     * @param layout The layout on which attach the [view]
     */
    fun attach(layout: FrameLayout) {
        parentView = layout

        layout.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
            )
        )
    }

    /**
     * Attach the [view] to a [RelativeLayout]
     *
     * @param layout The layout on which attach the [view]
     */
    fun attach(layout: RelativeLayout) {
        parentView = layout

        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        layout.addView(view, params)
    }

    /**
     *  Detach the [view] from the previously passed layout
     */
    fun detach() = parentView?.removeView(view)
}