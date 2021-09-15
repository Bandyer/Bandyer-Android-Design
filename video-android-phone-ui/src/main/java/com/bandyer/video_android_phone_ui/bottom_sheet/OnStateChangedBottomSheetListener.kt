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

package com.bandyer.video_android_phone_ui.bottom_sheet

/**
 * Listener of the status changes of the BottomSheet
 * @param T : BandyerBottomSheet
 */
interface OnStateChangedBottomSheetListener<T : BandyerBottomSheet> {

    /**
     * When the bottomSheet has been shown
     * @param bottomSheet T
     */
    fun onShow(bottomSheet: T)

    /**
     * When the bottomSheet has been hidden
     * @param bottomSheet T
     */
    fun onHide(bottomSheet: T)

    /**
     * When the bottomSheet has been collapsed
     * @param bottomSheet T
     */
    fun onCollapse(bottomSheet: T)

    /**
     * When the bottomSheet is being been dragged
     * @param bottomSheet T
     */
    fun onDragging(bottomSheet: T)

    /**
     * When the bottomSheet has been expanded
     * @param bottomSheet T
     */
    fun onExpand(bottomSheet: T)

    /**
     * When the bottomSheet has been anchored
     * @param bottomSheet T
     */
    fun onAnchor(bottomSheet: T)

    /**
     * When the bottomSheet is sliding
     * @param bottomSheet T
     */
    fun onSlide(bottomSheet: T, slideOffset: Float)
}