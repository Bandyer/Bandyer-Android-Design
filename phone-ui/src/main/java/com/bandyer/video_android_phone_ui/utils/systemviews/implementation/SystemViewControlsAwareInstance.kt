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

package com.bandyer.video_android_phone_ui.utils.systemviews.implementation

import com.bandyer.video_android_phone_ui.utils.systemviews.SystemViewLayoutObserver

/**
 * @suppress
 */
internal interface SystemViewControlsAwareInstance {

    /**
     * Add an observer to the system controls changes
     * @param observer SystemViewLayoutObserver
     * @param removeOnInsetChanged remove the observer as soon a Inset has been changed
     * @return SystemViewControlsAware
     */
    fun addObserver(observer: SystemViewLayoutObserver, removeOnInsetChanged: Boolean = false): SystemViewControlsAware

    /**
     * Remove the observer from the SystemViewControlsAware
     *
     * @param observer SystemViewLayoutObserver
     * @return SystemViewControlsAware
     */
    fun removeObserver(observer: SystemViewLayoutObserver): SystemViewControlsAware

    /**
     * Request to calculate the offsets
     */
    fun getOffsets()
}