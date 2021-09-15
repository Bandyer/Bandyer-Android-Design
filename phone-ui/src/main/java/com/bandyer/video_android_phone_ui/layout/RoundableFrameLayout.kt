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

package com.bandyer.video_android_phone_ui.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bandyer.android_common.RoundedView
import com.bandyer.video_android_phone_ui.R

/**
 * A frame layout that can round its corners using "round" and "corner_radius" roundable properties.
 * 
 * @constructor
 */
open class RoundableFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), RoundedView {

    init {
        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.RoundableStyle, defStyleAttr, 0)
            try {
                if (typedArray.getBoolean(R.styleable.RoundableStyle_bandyer_round, false))
                    round<RoundableFrameLayout>(true)
                else
                    typedArray.getDimensionPixelSize(R.styleable.RoundableStyle_bandyer_corner_radius, 0).takeIf { it > 0 }?.let {
                        setCornerRadius<RoundableFrameLayout>(it.toFloat())
                    }
            } finally {
                typedArray.recycle()
            }
        }
    }

    /**
     * @suppress
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setRoundClip(canvas)
    }
}