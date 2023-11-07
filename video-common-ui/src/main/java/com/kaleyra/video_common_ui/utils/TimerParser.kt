/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui.utils

/**
 * TimerParser
 */
object TimerParser {
    /**
     * Parse the timestamp into mm:ss or hh:mm:ss format
     *
     * @param timestamp Timestamp expressed in seconds
     */
    fun parseTimestamp(timestamp: Long): String {
        val hours = timestamp / 3600
        val minutes = (timestamp / 60) % 60
        val seconds = timestamp % 60
        return if (hours == 0L) String.format("%02d:%02d", minutes, seconds)
        else String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}