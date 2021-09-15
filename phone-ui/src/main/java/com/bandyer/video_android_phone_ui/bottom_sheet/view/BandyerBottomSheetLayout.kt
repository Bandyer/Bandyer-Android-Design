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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

/**
 * A custom Coordinator Layout
 * @author kristiyan
 */
open class BandyerBottomSheetLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : androidx.coordinatorlayout.widget.CoordinatorLayout(context, attrs, defStyleAttr) {

    /**
     * Hide the backgroundView hosted as reference in the BottomSheetLayoutContent.
     * The backgroundView's parent is an ancestor of this CoordinatorLayout.
     * @suppress
     */
    @SuppressLint("WrongViewCast")
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        for (i in 0..childCount) {
            val layout = getChildAt(i) as? BottomSheetLayoutContent
            layout?.backgroundView?.visibility = visibility
        }
    }
}