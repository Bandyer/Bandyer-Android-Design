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

import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.items.AdapterActionItem
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutType
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension

/**
 * This class represents a bottom sheet displaying selectable items.
 * @author kristiyan
 */
@Suppress("UNCHECKED_CAST")
open class BandyerSelectableBottomSheet<T : ActionItem>(
    context: AppCompatActivity,
    selection: Int = -1,
    views: List<T>,
    peekHeight: Int?,
    bottomSheetLayoutType: BottomSheetLayoutType,
    bottomSheetStyle: Int
) : BandyerActionBottomSheet<T>(context, views, peekHeight, bottomSheetLayoutType, bottomSheetStyle) {

    /**
     * Current item selected
     */
    protected var currentItemSelected: AdapterActionItem? = null

    init {

        val selectExtension = fastAdapter.getSelectExtension()
        selectExtension.isSelectable = true
        selectExtension.allowDeselection = false
        selectExtension.multiSelect = false
        selectExtension.selectWithItemUpdate = true
        selectExtension.selectionListener = object : ISelectionListener<AdapterActionItem> {
            override fun onSelectionChanged(item: AdapterActionItem, selected: Boolean) {
                if (!selected) return
                currentItemSelected = item
                notifyItemSelected(item, fastItemAdapter.adapterItems.indexOf(item))
            }
        }

        if (selection != -1) {
            fastAdapter.getExtension<SelectExtension<AdapterActionItem>>(SelectExtension::class.java)?.select(selection)
            currentItemSelected = fastItemAdapter.getAdapterItem(selection)
        }
    }

    private fun notifyItemSelected(item: AdapterActionItem, position: Int) {
        val listener = onActionBottomSheetListener as? OnActionBottomSheetListener<ActionItem, BandyerBottomSheet>
        listener?.onActionClicked(this, item.item, position)
    }

    /**
     * Select the item provided
     * @param actionItem ActionItem to select
     */
    open fun selectItem(actionItem: ActionItem?) {
        if (actionItem == null)
            return

        currentItemSelected = AdapterActionItem(actionItem)

        fastAdapter.getExtension<SelectExtension<AdapterActionItem>>(SelectExtension::class.java)?.deselect()

        val position = fastItemAdapter.adapterItems.indexOfFirst { it.item == actionItem }
        fastAdapter.getExtension<SelectExtension<AdapterActionItem>>(SelectExtension::class.java)?.select(position)
    }
}