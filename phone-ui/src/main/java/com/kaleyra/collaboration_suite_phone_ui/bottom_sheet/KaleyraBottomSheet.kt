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

import android.os.Bundle
import android.view.View
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.call.buttons.KaleyraLineButton
import com.google.android.material.textview.MaterialTextView

/**
 * **Kaleyra BottomSheet**
 * This bottomSheet is using a custom behaviour that enables a third state ( ANCHOR_POINT )
 * The bottomSheet style is composed of a line indicating the top of the sheet a title and a recyclerView
 */
interface KaleyraBottomSheet {

    /**
     * Current visibility status of the bottomSheet
     */
    var state: Int
    /**
     * The lineView of the bottomSheet
     */
    var lineView: KaleyraLineButton?
    /**
     * The titleView of the bottomSheet
     */
    var titleView: MaterialTextView?

    /**
     * The recyclerView of the bottomSheet
     */
    var recyclerView: androidx.recyclerview.widget.RecyclerView?

    /**
     * Listener for status changes
     */
    var onStateChangedBottomSheetListener: OnStateChangedBottomSheetListener<KaleyraBottomSheet>?

    /**
     * Show the bottomSheet
     */
    fun show()

    /**
     * Hide the bottomSheet
     * @param force true to hide no matter what
     */
    fun hide(force: Boolean = false)

    /**
     * Hide the bottomSheet
     */
    fun hide() {
        hide(false)
    }

    /**
     * Toggle the bottomSheet states
     */
    fun toggle()

    /**
     * Set to Expand visibility level
     */
    fun expand()

    /**
     * Set to AnchorPoint visibility level
     */
    fun anchor()

    /**
     * Set to Collapse visibility level
     * @param to View? where to collapse to
     * @param offset Int? additional offset to add
     */
    fun collapse(to: View?, offset: Int? = 0)

    /**
     * Destroy completely the bottomSheet
     */
    fun dispose()

    /**
     * Method to get visibility of bottomSheet
     * @return true if visible on screen, false otherwise
     */
    fun isVisible(): Boolean

    /**
     * Save the current configuration of the bottomSheet to be able to restore it later
     * @param saveInstanceState add additional data in the Bundle
     * @return the saved configuration Bundle
     */
    fun saveInstanceState(saveInstanceState: Bundle?): Bundle? {
        return saveInstanceState
    }

    /**
     * Save the current configuration of the bottomSheet to be able to restore it later
     * @param bundle add additional data in the Bundle
     */
    fun restoreInstanceState(bundle: Bundle?) {}

    /**
     * Set an item at position
     * @param item ActionItem to be set
     * @param position where to set the item
     */
    fun setItem(item: ActionItem, position: Int)

    /**
     * Update the item if present
     * @param item ActionItem to be updated
     */
    fun updateItem(item: ActionItem)

    /**
     * Get an item given a position
     * @param position of the item to get
     * @return ActionItem found at position null if not present
     */
    fun getItem(position: Int): ActionItem?

    /**
     * Set a list of items to be used
     * @param items List<ActionItem> to set
     */
    fun setItems(items: List<ActionItem>)

    /**
     * Get an item position
     * @param item ActionItem to find
     * @return position of the actionItem
     */
    fun getItemIndex(item: ActionItem): Int

    /**
     * Remove an item
     * @param item ActionItem to remove
     */
    fun removeItem(item: ActionItem)

    /**
     * Add a new item at position
     * @param item ActionItem to add
     * @param position position where to add
     */
    fun addItem(item: ActionItem, position: Int)

    /**
     * Gives the first item or null in case it's not found
     * @param actionItem Class<T> item to find
     * @return T? the first item found
     */
    fun <T : ActionItem> firstOrNull(actionItem: Class<T>): T?

    /**
     * Replace an item with another
     * @param old item to replace
     * @param new new item
     */
    fun replaceItems(old: ActionItem, new: ActionItem)
}