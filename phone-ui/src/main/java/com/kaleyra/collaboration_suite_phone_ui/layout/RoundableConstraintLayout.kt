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
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.kaleyra.collaboration_suite_utils.RoundedView
import com.kaleyra.collaboration_suite_phone_ui.R

/**
 * A constraint layout that can round its corners using "round" and "corner_radius" roundable properties.
 *
 * @constructor
 */
open class RoundableConstraintLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr), RoundedView {

    init {
        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.RoundableStyle, defStyleAttr, 0)
            try {
                if (typedArray.getBoolean(R.styleable.RoundableStyle_kaleyra_round, false))
                    round<RoundableFrameLayout>(true)
                else
                    typedArray.getDimensionPixelSize(R.styleable.RoundableStyle_kaleyra_corner_radius, 0).takeIf { it > 0 }?.let {
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