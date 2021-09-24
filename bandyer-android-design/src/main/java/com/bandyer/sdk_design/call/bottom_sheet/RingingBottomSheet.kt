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

package com.bandyer.sdk_design.call.bottom_sheet

import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.bottom_sheet.BandyerClickableBottomSheet
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutType
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction

/**
 * Ringing bottom sheet
 * @param T action item type
 * @property bottomSheetLayoutType BottomSheetLayoutType
 * @constructor
 */
@Suppress("UNCHECKED_CAST")
open class RingingBottomSheet<T>(
    context: AppCompatActivity,
    bottomSheetLayoutType: BottomSheetLayoutType,
    bottomSheetStyle: Int
) : BandyerClickableBottomSheet<T>(
    context,
    CallAction.getIncomingCallActions(context) as List<T>,
    0,
    bottomSheetLayoutType,
    bottomSheetStyle
) where T : ActionItem {

    override fun show() {
        super.show()
        bottomSheetBehaviour!!.isHideable = false
        bottomSheetBehaviour!!.skipCollapsed = true
        bottomSheetBehaviour!!.disableDragging = true
        expand()
    }
}