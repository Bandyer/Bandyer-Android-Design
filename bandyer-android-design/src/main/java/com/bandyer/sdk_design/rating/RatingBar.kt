package com.bandyer.sdk_design.rating

import androidx.annotation.FloatRange
import androidx.annotation.IntRange

/*
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
    }
}