package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.util.AttributeSet
import com.bandyer.sdk_design.R
import com.google.android.material.imageview.ShapeableImageView

class BandyerFileTypeImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShapeableImageView(context, attrs, defStyleAttr) {

    var type: Type? = Type.UNDEFINED
        set(value) {
            field = value
            refreshDrawableState()
        }

    /**
     * @suppress
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 5)
        val type = type ?: return drawableState
        return mergeDrawableStates(drawableState, type.value)
    }

    /**
     * Enum representing file type
     *
     * @param value state drawable resource
     * @constructor
     */
    enum class Type(val value: IntArray) {

        /**
         * d o c
         */
        DOC(intArrayOf(R.attr.bandyer_state_doc)),

        /**
         * i m a g e
         */
        IMAGE(intArrayOf(R.attr.bandyer_state_image)),

        /**
         * v i d e o
         */
        VIDEO(intArrayOf(R.attr.bandyer_state_video)),

        /**
         * a r c h i v e
         */
        ARCHIVE(intArrayOf(R.attr.bandyer_state_archive)),

        /**
         * u n d e f i n e d
         */
        UNDEFINED(intArrayOf(R.attr.bandyer_state_undefined))
    }

}