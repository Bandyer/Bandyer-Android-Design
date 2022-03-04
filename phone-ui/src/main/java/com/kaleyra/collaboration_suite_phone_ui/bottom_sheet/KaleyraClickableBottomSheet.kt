/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.bottom_sheet

import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.AdapterActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutType
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

/**
 * This class represents a bottom sheet displaying clickable items.
 * @author kristiyan
 */
@Suppress("UNCHECKED_CAST")
abstract class KaleyraClickableBottomSheet<T> constructor(
    context: AppCompatActivity,
    views: List<T>,
    peekHeight: Int?,
    bottomSheetLayoutType: BottomSheetLayoutType,
    @StyleRes bottomSheetLayoutStyle: Int
) : KaleyraActionBottomSheet<T>(context, views, peekHeight, bottomSheetLayoutType, bottomSheetLayoutStyle) where T : ActionItem {

    init {
        fastAdapter.addEventHook(object : ClickEventHook<AdapterActionItem>() {
            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<AdapterActionItem>, item: AdapterActionItem) {
                if (item.item.itemView == null) return
                val listener = onActionBottomSheetListener as? OnActionBottomSheetListener<ActionItem, KaleyraBottomSheet>
                listener?.onActionClicked(this@KaleyraClickableBottomSheet, item.item as T, position)
            }

            override fun onBindMany(viewHolder: RecyclerView.ViewHolder): MutableList<View> = getClickableViews(viewHolder)
        })
    }

    /**
     * Returns a list of views that triggers the click selection on the input view holder
     * @param viewHolder ViewHolder input view holder
     * @return MutableList<View> the list of clickable views
     */
    abstract fun getClickableViews(viewHolder: RecyclerView.ViewHolder): MutableList<View>
}