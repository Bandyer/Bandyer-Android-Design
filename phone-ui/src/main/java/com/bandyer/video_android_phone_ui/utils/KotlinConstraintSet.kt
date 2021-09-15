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

package com.bandyer.video_android_phone_ui.utils;

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager


/**
 * ConstraintSet Utility
 *
 * @constructor Create
 */
internal class KotlinConstraintSet : ConstraintSet() {

    companion object {
        inline fun ConstraintLayout.changeConstraints(block: KotlinConstraintSet.() -> Unit) {
            KotlinConstraintSet().also { it.clones(this) }.apply(block).appliesTo(this)
        }
    }

    var margin: Int? = null
        get() {
            val result = field
            margin = null
            return result
        }

    var transition = false

    infix fun Int.topToBottomOf(bottom: Int) = margin?.let {
        connect(this, TOP, bottom, BOTTOM, it)
    } ?: connect(this, TOP, bottom, BOTTOM)

    infix fun Int.bottomToBottomOf(bottom: Int) = margin?.let {
        connect(this, BOTTOM, bottom, BOTTOM, it)
    } ?: connect(this, BOTTOM, bottom, BOTTOM)

    infix fun Int.topToTopOf(top: Int) = margin?.let {
        connect(this, TOP, top, TOP, it)
    } ?: connect(this, TOP, top, TOP)

    infix fun Int.startToEndOf(end: Int) = margin?.let {
        connect(this, START, end, END, it)
    } ?: connect(this, START, end, END)

    infix fun Int.startToStartOf(end: Int) = margin?.let {
        connect(this, START, end, START, it)
    } ?: connect(this, START, end, START)

    infix fun Int.endToEndOf(end: Int) = margin?.let {
        connect(this, END, end, END, it)
    } ?: connect(this, END, end, END)

    infix fun Int.guidePercentTo(end: Float) = setGuidelinePercent(this, end)

    infix fun Int.clear(constraint: Constraints) = when (constraint) {
        Constraints.TOP -> clear(this, TOP)
        Constraints.BOTTOM -> clear(this, BOTTOM)
        Constraints.END -> clear(this, END)
        Constraints.START -> clear(this, START)
        Constraints.RIGHT -> clear(this, RIGHT)
        Constraints.LEFT -> clear(this, LEFT)
    }

    private infix fun beginDelayedTransition(constraintLayout: ConstraintLayout) = TransitionManager.beginDelayedTransition(constraintLayout)

    infix fun appliesTo(constraintLayout: ConstraintLayout) {
        if (transition) this beginDelayedTransition constraintLayout
        applyTo(constraintLayout)
    }

    infix fun clones(constraintLayout: ConstraintLayout) = clone(constraintLayout)


}
internal enum class Constraints {
    TOP, BOTTOM, START, END, RIGHT, LEFT
}