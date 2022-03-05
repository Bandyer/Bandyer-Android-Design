/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.layout

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.widget.FrameLayout
import com.kaleyra.collaboration_suite_core_ui.extensions.ViewExtensions.setPaddingEnd
import com.kaleyra.collaboration_suite_core_ui.extensions.ViewExtensions.setPaddingLeft
import com.kaleyra.collaboration_suite_core_ui.extensions.ViewExtensions.setPaddingRight
import com.kaleyra.collaboration_suite_core_ui.extensions.ViewExtensions.setPaddingStart
import com.kaleyra.collaboration_suite_phone_ui.extensions.*
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutObserver
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutOffsetListener

/**
 * A FrameLayout which follows is aware of the system views dimensions and positions
 *
 * @constructor
 */
open class SystemControlsAwareHorizontalFrameLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), SystemViewLayoutObserver {


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