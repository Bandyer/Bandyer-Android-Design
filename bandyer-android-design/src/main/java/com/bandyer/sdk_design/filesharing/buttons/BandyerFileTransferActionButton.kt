package com.bandyer.sdk_design.filesharing.buttons

import android.content.Context
import android.util.AttributeSet
import com.bandyer.sdk_design.R
import com.google.android.material.button.MaterialButton

/**
 * A button which represent the performable action on the file transfer
 */
class BandyerFileTransferActionButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialButton(context, attrs, defStyleAttr) {

    /**
     * The type of the button. It changes the drawable state and the content description
     */
    var type: Type? = Type.RETRY
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
            Type.CANCEL -> resources.getString(R.string.bandyer_fileshare_cancel)
            Type.DOWNLOAD -> resources.getString(R.string.bandyer_fileshare_download_descr)
            Type.SUCCESS  -> resources.getString(R.string.bandyer_fileshare_success)
            else          -> resources.getString(R.string.bandyer_fileshare_retry)
        }
    }

    /**
     * Enum representing action button type
     *
     * @param value state drawable resource
     * @constructor
     */
    enum class Type(val value: IntArray) {

        /**
         * c a n c e l
         */
        CANCEL(intArrayOf(R.attr.bandyer_state_cancel)),

        /**
         * d o w n l o a d
         */
        DOWNLOAD(intArrayOf(R.attr.bandyer_state_download)),

        /**
         * r e d o w n l o a d
         */
        SUCCESS(intArrayOf(R.attr.bandyer_state_success)),

        /**
         * r e t r y
         */
        RETRY(intArrayOf(R.attr.bandyer_state_retry))
    }
}