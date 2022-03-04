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

package com.bandyer.video_android_core_ui.extensions

import android.graphics.Color
import java.math.BigInteger
import java.security.MessageDigest

/**
 * String extensions
 */
object StringExtensions {

    /**
     * Return a color based on the given a string
     *
     * @receiver The string
     * @return The color
     */
    fun String.parseToColor(): Int {
        val md = MessageDigest.getInstance("MD5")
        val md5 = BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0').substring(0, 8).takeLast(6)
        return Color.parseColor("#$md5")
    }
}