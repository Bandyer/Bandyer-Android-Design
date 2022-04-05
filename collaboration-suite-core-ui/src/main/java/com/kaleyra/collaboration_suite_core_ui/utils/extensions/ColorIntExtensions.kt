/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils.extensions

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Color Int extensions
 */
object ColorIntExtensions {

    /**
     * Tell if a color needs to be coupled with a light color
     * @receiver Int the IntColor
     * @return true if the color needs a light color
     */
    fun @receiver:androidx.annotation.ColorInt Int.requiresLightColor(): Boolean {
        val red = Color.red(this)
        val green = Color.green(this)
        val blue = Color.blue(this)
        val yiq = ((red * 299) + (green * 587) + (blue * 114)) / 1000
        return yiq < 192
    }

    /**
     * Darken the receiver color by a given factor
     *
     * @receiver The target color
     * @param darkenFactor Float
     * @return The darkened color
     */
    @ColorInt
    fun @receiver:ColorInt Int.darkenColor(darkenFactor: Float): Int {
        return Color.HSVToColor(FloatArray(3).apply {
            Color.colorToHSV(this@darkenColor, this)
            this[2] *= darkenFactor
        })
    }
}