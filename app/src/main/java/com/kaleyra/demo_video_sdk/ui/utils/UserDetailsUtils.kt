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