package com.bandyer.sdk_design.feedback

import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.core.graphics.drawable.DrawableCompat
import com.bandyer.sdk_design.databinding.BandyerRatingBarElementBinding

/**
 * A BaseRatingBarElement
 */
internal class BaseRatingBarElement : FrameLayout {

    private var _binding: BandyerRatingBarElementBinding? = null
    private val binding: BandyerRatingBarElementBinding
        get() = _binding!!

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, progressDrawable: Drawable, backgroundDrawable: Drawable, iconSize: Int, @Px padding: Int) : super(context) {
        _binding = BandyerRatingBarElementBinding.inflate(LayoutInflater.from(context), this, true)

        layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f)

        val size = if (iconSize == 0) LayoutParams.WRAP_CONTENT else iconSize
        val params = LayoutParams(size, size).apply { gravity = Gravity.CENTER_HORIZONTAL }

        with(binding.bandyerProgressImage) {
            layoutParams = params
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            if(progressDrawable.constantState == null) return@with
            setImageDrawable(ClipDrawable(progressDrawable.constantState!!.newDrawable(), Gravity.START, ClipDrawable.HORIZONTAL))
        }

        with(binding.bandyerBackgroundImage)  {
            layoutParams = params
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            if(backgroundDrawable.constantState == null) return@with
            setImageDrawable(ClipDrawable(backgroundDrawable.constantState!!.newDrawable(), Gravity.END, ClipDrawable.HORIZONTAL))
        }

        setProgress(0f)
        setPadding(padding, padding, padding, padding)
    }

    /**
     * Set the percentage progress for the item
     *
     * @param rating Float
     */
    fun setProgress(@FloatRange(from = 0.0, to = 1.0) rating: Float) = with(binding) {
        val level = (MAX_IMAGE_LEVEL * rating).toInt()
        bandyerProgressImage.setImageLevel(level)
        bandyerBackgroundImage.setImageLevel(MAX_IMAGE_LEVEL - level)
    }

    private companion object {
        const val MAX_IMAGE_LEVEL = 10000
    }
}