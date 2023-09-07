package com.kaleyra.collaboration_suite_phone_ui.call

import android.content.res.Configuration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object ConfigurationExtensions {

    val MediumSizeWidth = 600.dp

    val MediumSizeHeight = 480.dp

    fun Configuration.isOrientationPortrait() = orientation == Configuration.ORIENTATION_PORTRAIT

    fun Dp.isAtLeastMediumSizeWidth() = this >= MediumSizeWidth
    
    fun Dp.isAtLeastMediumSizeHeight() = this >= MediumSizeHeight

    fun isAtLeastMediumSizeDevice(width: Dp, height: Dp) = width.isAtLeastMediumSizeWidth() && height.isAtLeastMediumSizeHeight()

}