/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.filesharing.imageviews

import android.content.Context
import android.util.AttributeSet
import com.bandyer.sdk_design.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * This ImageView defines the transfer type (either upload or download)
 */
class BandyerFileTypeImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShapeableImageView(context, attrs, defStyleAttr) {

    /**
     * The type of the ImageView. It changes the drawable state and the content description
     */
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