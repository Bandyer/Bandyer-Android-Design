package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.util.AttributeSet
import com.bandyer.sdk_design.R
import com.google.android.material.imageview.ShapeableImageView

class BandyerFileTypeImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShapeableImageView(context, attrs, defStyleAttr) {

    var type: Type? = Type.FILE
        set(value) {
            field = value
            setContentDescription(value)
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

    private fun setContentDescription(value: Type?) {
        value ?: return
        contentDescription = when (value) {
            Type.FILE -> resources.getString(R.string.bandyer_fileshare_file)
            Type.IMAGE -> resources.getString(R.string.bandyer_fileshare_media)
            else -> resources.getString(R.string.bandyer_fileshare_archive)
        }
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
        FILE(intArrayOf(R.attr.bandyer_state_file)),

        /**
         * m e d i a
         */
        IMAGE(intArrayOf(R.attr.bandyer_state_media)),

        /**
         * a r c h i v e
         */
        ARCHIVE(intArrayOf(R.attr.bandyer_state_archive)),
    }

}