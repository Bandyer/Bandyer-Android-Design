package com.bandyer.sdk_design.new_smartglass

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.bandyer.sdk_design.extensions.dp2px

/**
 * A view aggregator of [ContactAvatarView]
 *
 * @constructor
 */
class ContactAvatarGroupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    /**
     * Add an avatar
     *
     * @param imageResId The local resource for the avatar
     */
    fun addAvatar(@DrawableRes imageResId: Int) = addAvatar().setImage(imageResId)

    /**
     * Add an avatar
     *
     * @param url The url remote resource for the avatar
     */
    fun addAvatar(url: String) = addAvatar().setImage(url)

    /**
     * Add an avatar
     *
     * @param text The avatar text, usually a letter
     * @param color The background color
     */
    fun addAvatar(text: String, @ColorInt color: Int?) {
        addAvatar().also {
            it.setText(text)
            it.setBackground(color)
        }
    }

    private fun addAvatar(): ContactAvatarView {
        val set = ConstraintSet()

        val elevation = - childCount
        val marginStart = if (childCount == 0) 0 else CHILD_START_MARGIN
        val startId = this.getChildAt(childCount - 1)?.id ?: this.id

        // Create the child view
        val child = ContactAvatarView(context)
        child.id = generateViewId()
        child.clipToPadding = false
        child.elevation = context.dp2px(elevation.toFloat()).toFloat()
        this.addView(child, childCount)

        // Set the constraints
        set.clone(this)
        set.connect(
            child.id, ConstraintSet.START, startId, ConstraintSet.START, context.dp2px(
                marginStart.toFloat()
            )
        )
        set.connect(
            child.id, ConstraintSet.TOP, this.id, ConstraintSet.TOP, 0
        )
        set.applyTo(this)

        return child
    }

    fun clean() = removeAllViews()

    private companion object {
        const val CHILD_START_MARGIN = 16
    }
}