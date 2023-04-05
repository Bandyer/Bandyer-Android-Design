/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.filesharing.imageviews

import android.content.Context
import android.util.AttributeSet

import com.kaleyra.collaboration_suite_phone_ui.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * This ImageView defines the transfer type (either upload or download)
 */
class KaleyraTransferTypeImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShapeableImageView(context, attrs, defStyleAttr) {

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
            Type.UPLOAD -> resources.getString(R.string.kaleyra_fileshare_upload)
            else -> resources.getString(R.string.kaleyra_fileshare_download)
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
        UPLOAD(intArrayOf(R.attr.kaleyra_state_upload)),

        /**
         * d o w n l o a d
         */
        DOWNLOAD(intArrayOf(R.attr.kaleyra_state_download))
    }
}