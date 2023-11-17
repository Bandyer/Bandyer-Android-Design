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

package com.kaleyra.demo_video_sdk.ui.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.kaleyra.demo_video_sdk.R.drawable
import com.kaleyra.video_common_ui.model.UserDetails
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

object UserDetailsUtils {
    suspend fun getUserImageBitmap(userDetails: UserDetails): Bitmap = userDetails.image.toBitmap() ?: fallbackUserBitmapIcon

    @JvmStatic
    private val fallbackUserBitmapIcon: Bitmap
        get() = vectorDrawableToBitmap(drawable.kaleyra_z_user_1, Color.LTGRAY)!!
}