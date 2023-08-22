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

package com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraClickableBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutType
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items.CallAction

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
) : KaleyraClickableBottomSheet<T>(
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

    override fun getClickableViews(viewHolder: RecyclerView.ViewHolder): MutableList<View> {
        val listOfViews = mutableListOf<View>()
        viewHolder.itemView.findViewById<View?>(R.id.kaleyra_button_view)?.let { listOfViews.add(it) }
        viewHolder.itemView.findViewById<View?>(R.id.kaleyra_label_view)?.let { listOfViews.add(it) }
        return listOfViews
    }
}