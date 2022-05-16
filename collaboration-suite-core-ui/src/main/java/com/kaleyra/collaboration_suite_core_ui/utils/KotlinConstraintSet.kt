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

package com.kaleyra.collaboration_suite_core_ui.utils

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager

/**
 * ConstraintSet Utility
 *
 * @constructor Create
 */
class KotlinConstraintSet : ConstraintSet() {

    /**
     * @suppress
     */
    companion object {
        /**
         *
         * @receiver ConstraintLayout
         * @param block [@kotlin.ExtensionFunctionType] Function1<KotlinConstraintSet, Unit>
         */
        inline fun ConstraintLayout.changeConstraints(block: KotlinConstraintSet.() -> Unit) {
            KotlinConstraintSet().also { it.clones(this) }.apply(block).appliesTo(this)
        }
    }

    /**
     * The view's margin
     */
    var margin: Int? = null
        get() {
            val result = field
            margin = null
            return result
        }

    /**
     * Transition flag, true to enable transitions, false otherwise
     */
    var transition = false

    /**
     * Set the view's top constraint to bottom of another view
     *
     * @receiver Int The target view's id to which apply the constraint
     * @param bottom The other view's id
     */
    infix fun Int.topToBottomOf(bottom: Int) = margin?.let {
        connect(this, TOP, bottom, BOTTOM, it)
    } ?: connect(this, TOP, bottom, BOTTOM)

    /**
     * Set the view's bottom constraint to top of another view
     *
     * @receiver Int The target view's id to which apply the constraint
     * @param top The other view's id
     */
    infix fun Int.bottomToTopOf(top: Int) = margin?.let {
        connect(this, BOTTOM, top, TOP, it)
    } ?: connect(this, BOTTOM, top, TOP)

    /**
     * Set the view's bottom constraint to bottom of another view
     *
     * @receiver Int The target view's id to which apply the constraint
     * @param bottom The other view's id
     */
    infix fun Int.bottomToBottomOf(bottom: Int) = margin?.let {
        connect(this, BOTTOM, bottom, BOTTOM, it)
    } ?: connect(this, BOTTOM, bottom, BOTTOM)

    /**
     * Set the view's top constraint to top of another view
     *
     * @receiver Int The target view's id to which apply the constraint
     * @param top The other view's id
     */
    infix fun Int.topToTopOf(top: Int) = margin?.let {
        connect(this, TOP, top, TOP, it)
    } ?: connect(this, TOP, top, TOP)

    /**
     * Set the view's start constraint to end of another view
     *
     * @receiver Int The target view's id to which apply the constraint
     * @param end The other view's id
     */
    infix fun Int.startToEndOf(end: Int) = margin?.let {
        connect(this, START, end, END, it)
    } ?: connect(this, START, end, END)

    /**
     * Set the view's start constraint to start of another view
     *
     * @receiver Int The target view's id to which apply the constraint
     * @param end The other view's id
     */
    infix fun Int.startToStartOf(end: Int) = margin?.let {
        connect(this, START, end, START, it)
    } ?: connect(this, START, end, START)

    /**
     * Set the view's end constraint to end of another view
     *
     * @receiver Int The target view's id to which apply the constraint
     * @param end The other view's id
     */
    infix fun Int.endToEndOf(end: Int) = margin?.let {
        connect(this, END, end, END, it)
    } ?: connect(this, END, end, END)

    /**
     * Set the view's end constraint to start of another view
     *
     * @receiver Int The target view's id to which apply the constraint
     * @param start The other view's id
     */
    infix fun Int.endToStartOf(start: Int) = margin?.let {
        connect(this, END, start, START, it)
    } ?: connect(this, END, start, START)

    /**
     * Set the guideline percentage of a view to the defined value
     *
     * @receiver Int The view's id
     * @param end The percentage to which set the guideline
     */
    infix fun Int.guidePercentTo(end: Float) = setGuidelinePercent(this, end)

    /**
     * Clear a constraint of a view
     *
     * @receiver Int The view's id
     * @param constraint The constraint to remove
     */
    infix fun Int.clear(constraint: Constraints) = when (constraint) {
        Constraints.TOP -> clear(this, TOP)
        Constraints.BOTTOM -> clear(this, BOTTOM)
        Constraints.END -> clear(this, END)
        Constraints.START -> clear(this, START)
        Constraints.RIGHT -> clear(this, RIGHT)
        Constraints.LEFT -> clear(this, LEFT)
    }

    /**
     * Animate to a new scene defined by all changes within the given scene root between calling this method and the next rendering frame
     *
     * @param constraintLayout ConstraintLayout
     */
    private infix fun beginDelayedTransition(constraintLayout: ConstraintLayout) =
        TransitionManager.beginDelayedTransition(constraintLayout)

    /**
     * Apply constraints to a ConstraintLayout,
     *
     * @param constraintLayout ConstraintLayout
     */
    infix fun appliesTo(constraintLayout: ConstraintLayout) {
        if (transition) this beginDelayedTransition constraintLayout
        applyTo(constraintLayout)
    }

    /**
     * Copy the layout parameters of a ConstraintLayout
     *
     * @param constraintLayout ConstraintLayout
     */
    infix fun clones(constraintLayout: ConstraintLayout) = clone(constraintLayout)
}

/**
 * Constraint enum class
 */
enum class Constraints {
    /**
     * t o p
     */
    TOP,
    /**
     * b o t t o m
     */
    BOTTOM,
    /**
     * s t a r t
     */
    START,
    /**
     * e n d
     */
    END,
    /**
     * r i g h t
     */
    RIGHT,
    /**
     * l e f t
     */
    LEFT
}