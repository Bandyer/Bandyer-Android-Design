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

package com.kaleyra.collaboration_suite_phone_ui.feedback

import androidx.annotation.FloatRange
import androidx.annotation.IntRange

/**
 * A generic interface for a RatingBar
 */
interface RatingBar {

    /**
     * Set the number of levels of the rating bar
     *
     * @param numLevels Int
     */
    fun setNumLevels(@IntRange(from = 0) numLevels: Int)

    /**
     * Returns the number of levels of the rating bar
     *
     * @return Float
     */
    fun getNumLevels(): Int

    /**
     * Set the rating value
     *
     * @param value Float
     */
    fun setRating(value: Float)


    /**
     * Returns the rating value
     *
     * @return Float
     */
    fun getRating(): Float

    /**
     * Set the rating's step size
     *
     * @param stepSize Float
     */
    fun setStepSize(@FloatRange(from = 0.1, to = 1.0) stepSize: Float)

    /**
     * Returns the rating's step size
     *
     * @return Float
     */
    fun getStepSize(): Float

    /**
     * OnRatingChangeListener
     */
    interface OnRatingChangeListener {
        /**
         * Called when the rating value changes
         *
         * @param rating Float
         */
        fun onRatingChange(rating: Float)

        /**
         * Called after the user has interacted with the rating bar
         *
         * @param rating Float
         */
        fun onRatingConfirmed(rating: Float)
    }
}