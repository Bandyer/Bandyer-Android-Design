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

package com.bandyer.sdk_design.layout

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.extensions.*
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutObserver
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutOffsetListener

/**
 * A ConstraintLayout which follows is aware of the system views dimensions and positions
 *
 * @constructor
 */
open class SystemControlsAwareHorizontalConstraintLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), SystemViewLayoutObserver {


    ///////////////////////////////////////// SYSTEM CONTROLS AWARE OBSERVER //////////////////////////////////////////////////////////////////////////////////

    /**
     * @suppress
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.addObserver(it, this)
        }
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.removeObserver(it as AppCompatActivity, this)
        }
    }

    /**
     * @suppress
     */
    override fun onTopInsetChanged(pixels: Int) {}

    /**
     * @suppress
     */
    override fun onBottomInsetChanged(pixels: Int) {}

    /**
     * @suppress
     */
    override fun onRightInsetChanged(pixels: Int) {
        post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                setPaddingEnd(pixels)
            else
                setPaddingRight(pixels)
        }
    }

    /**
     * @suppress
     */
    override fun onLeftInsetChanged(pixels: Int) {
        post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                setPaddingStart(pixels)
            else
                setPaddingLeft(pixels)
        }
    }
}