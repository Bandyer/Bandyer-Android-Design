package com.bandyer.video_android_phone_ui.filesharing.imageviews

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_phone_ui.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * This ImageView defines the transfer type (either upload or download)
 */
class BandyerTransferTypeImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShapeableImageView(context, attrs, defStyleAttr) {

    /**
     * The type of the ImageView. It changes the drawable state and the content description
     */
    var type: Type? = Type.DOWNLOAD
        set(value) {
            field = value
            setContentDescription(value)
            refreshDrawableState()
        }

    /**
     * @suppress
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        val type = type ?: return drawableState
        return mergeDrawableStates(drawableState, type.value)
    }

    private fun setContentDescription(value: Type?) {
        value ?: return
        contentDescription = when (value) {
            Type.UPLOAD -> resources.getString(R.string.bandyer_fileshare_upload)
            else -> resources.getString(R.string.bandyer_fileshare_download)
        }
    }

    /**
     * Enum representing operation type
     *
     * @param value state drawable resource
     * @constructor
     */
    enum class Type(val value: IntArray) {

        /**
         * u p l o a d
         */
        UPLOAD(intArrayOf(R.attr.bandyer_state_upload)),

        /**
         * d o w n l o a d
         */
        DOWNLOAD(intArrayOf(R.attr.bandyer_state_download))
    }
}