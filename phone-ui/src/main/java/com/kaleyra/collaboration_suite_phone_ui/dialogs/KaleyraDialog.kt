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

package com.kaleyra.collaboration_suite_phone_ui.dialogs

import android.content.DialogInterface
import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheetDialog

/**
 * Interface defining a Kaleyra dialog
 * @author kristiyan
 */
interface KaleyraDialog<T : KaleyraBottomSheetDialog> {

    /**
     * Identifier for the dialog
     */
    val id: String

    /**
     * If the dialog is visible or not
     */
    val isVisible
        get() = dialog?.isVisible == true

    /**
     * Dialog that will be handled
     */
    var dialog: T?

    /**
     * Call this method to show the dialog
     * @param activity Activity
     */
    fun show(activity: FragmentActivity)

    /**
     * Call this method to dismiss the dialog
     */
    fun dismiss() {
        if (dialog?.fragmentManager != null) dialog?.dismissAllowingStateLoss()
    }

    /**
     * Set a dismiss listener to be notified when the dialog has been dismissed
     * @param listener DialogInterface.OnDismissListener
     */
    fun setOnDismissListener(listener: DialogInterface.OnDismissListener) {
        dialog?.setDismissListener(listener)
    }

}