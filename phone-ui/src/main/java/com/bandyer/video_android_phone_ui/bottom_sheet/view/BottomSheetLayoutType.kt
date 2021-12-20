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

package com.bandyer.video_android_phone_ui.bottom_sheet.view

/**
 * Bandyer bottom sheet layout types supported
 * @constructor
 */
sealed class BottomSheetLayoutType {

    /**
     * Bandyer bottom sheet layout orientation
     */
    enum class Orientation {
        /**
         * Horizontal
         */
        HORIZONTAL,

        /**
         * Vertical
         */
        VERTICAL
    }

    /**
     * Orientation
     */
    abstract val orientation: Orientation

    /**
     * Grid bottom sheet layout type
     * @property spanSize Int grid span size
     * @constructor
     */
    class GRID(val spanSize: Int, override val orientation: Orientation) : BottomSheetLayoutType() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GRID) return false

            if (spanSize != other.spanSize) return false
            if (orientation != other.orientation) return false

            return true
        }

        /**
         * @supperss
         */
        override fun hashCode(): Int = super.hashCode()
    }

    /**
     * List bottom sheet layout type
     * @constructor
     */
    class LIST(override val orientation: Orientation) : BottomSheetLayoutType() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is LIST) return false

            if (orientation != other.orientation) return false

            return true
        }

        /**
         * @supperss
         */
        override fun hashCode(): Int = super.hashCode()
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BottomSheetLayoutType) return false

        if (orientation != other.orientation) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int = javaClass.hashCode()
}