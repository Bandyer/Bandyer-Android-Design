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

package com.bandyer.sdk_design.bottom_sheet

import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.items.AdapterActionItem
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutType
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

/**
 * This class represents a bottom sheet displaying clickable items.
 * @author kristiyan
 */
@Suppress("UNCHECKED_CAST")
open class BandyerClickableBottomSheet<T> constructor(context: AppCompatActivity,
                                                      views: List<T>,
                                                      spanSize: Int,
                                                      peekHeight: Int?,
                                                      bottomSheetLayoutType: BottomSheetLayoutType,
                                                      @StyleRes bottomSheetLayoutStyle: Int) : BandyerActionBottomSheet<T>(context, views, spanSize, peekHeight, bottomSheetLayoutType, bottomSheetLayoutStyle) where T : ActionItem {

    init {
        fastAdapter.withEventHook(object : ClickEventHook<AdapterActionItem>() {
            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<AdapterActionItem>, item: AdapterActionItem) {
                if (item.item.itemView == null) return
                val listener = onActionBottomSheetListener as? OnActionBottomSheetListener<ActionItem, BandyerBottomSheet>
                listener?.onActionClicked(this@BandyerClickableBottomSheet, item.item as T, position)
            }

            override fun onBindMany(viewHolder: RecyclerView.ViewHolder): MutableList<View> {
                val listOfViews = mutableListOf<View>()
                viewHolder.itemView.findViewById<View?>(R.id.bandyer_button_view)?.let { listOfViews.add(it) }
                viewHolder.itemView.findViewById<View?>(R.id.bandyer_label_view)?.let { listOfViews.add(it) }
                return listOfViews
            }
        })
    }
}