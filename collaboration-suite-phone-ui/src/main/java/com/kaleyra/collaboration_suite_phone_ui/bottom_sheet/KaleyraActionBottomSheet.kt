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

package com.kaleyra.collaboration_suite_phone_ui.bottom_sheet

import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutType

/**
 * A Kaleyra BottomSheet specialized in handling actions
 * @author kristiyan
 */
open class KaleyraActionBottomSheet<T> constructor(context: AppCompatActivity,
                                                   views: List<T>,
                                                   peekHeight: Int?,
                                                   bottomSheetLayoutType: BottomSheetLayoutType,
                                                   @StyleRes bottomSheetLayoutStyle: Int) : BaseKaleyraBottomSheet(context, views, peekHeight, bottomSheetLayoutType, bottomSheetLayoutStyle) where T : ActionItem {

    /**
     * Listener for actions on the items of the bottomSheet
     */
    var onActionBottomSheetListener: OnActionBottomSheetListener<*, *>? = null

    /**
     * Generic interface handling action events
     *
     * @param T ActionItem where the event has been fired from
     * @param F BottomSheet enclosing the actions
     * @constructor Create On action bottom sheet listener
     */
    fun interface OnActionBottomSheetListener<T, F> where T : ActionItem, F : KaleyraBottomSheet {

        /**
         * Called when an action has received a click event
         * @param bottomSheet F bottomSheet container
         * @param action T action clicked
         * @param position Int position of item in the bottomSheet
         * @return true if handled false otherwise
         */
        fun onActionClicked(bottomSheet: F, action: T, position: Int): Boolean
    }
}