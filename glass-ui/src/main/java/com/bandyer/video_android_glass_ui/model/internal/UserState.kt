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

package com.bandyer.video_android_glass_ui.model.internal

/**
 * The user online state
 */
internal sealed class UserState {

    object Online : UserState()

    object Offline : UserState()

    data class Invited(val isOnline: Boolean) : UserState() {
        companion object : UserState() {
            override fun hashCode(): Int = "Invited".hashCode()
            override fun equals(other: Any?) = other is Invited
            override fun toString() = "Invited"
        }
    }
}