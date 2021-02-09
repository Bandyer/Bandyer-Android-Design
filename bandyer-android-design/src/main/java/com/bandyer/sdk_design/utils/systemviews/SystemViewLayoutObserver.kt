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

package com.bandyer.sdk_design.utils.systemviews

import androidx.annotation.Px

/**
 * System views observer which notifies the space a system view is taking off the screen
 */
interface SystemViewLayoutObserver {

    /**
     * Called when a system view is taking space from the top
     *
     * @param pixels height of the system view in pixels
     */
    fun onTopInsetChanged(@Px pixels: Int)

    /**
     * Called when a system view is taking space from the bottom
     *
     * @param pixels height of the system view in pixels
     */
    fun onBottomInsetChanged(@Px pixels: Int)

    /**
     * Called when a system view is taking space from the right
     *
     * @param pixels width of the system view in pixels
     */
    fun onRightInsetChanged(@Px pixels: Int)

    /**
     * Called when a system view is taking space from the left
     *
     * @param pixels width of the system view in pixels
     */
    fun onLeftInsetChanged(@Px pixels: Int)

}