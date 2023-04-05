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

package com.kaleyra.collaboration_suite_phone_ui.screensharing.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheetDialog
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.behaviours.KaleyraBottomSheetBehaviour
import com.kaleyra.collaboration_suite_phone_ui.dialogs.KaleyraDialog
import com.kaleyra.collaboration_suite_phone_ui.extensions.getCallThemeAttribute

/**
 * Kaleyra screen share picker dialog
 *
 * @constructor Create empty Kaleyra screen share picker dialog
 */
class KaleyraScreenSharePickerDialog : KaleyraDialog<KaleyraBottomSheetDialog> {

    override var dialog: KaleyraBottomSheetDialog? = null

    override val id: String = "KaleyraScreenSharePickerDialog"

    /**
     * Sharing option
     *
     * @constructor Create empty Sharing option
     */
    enum class SharingOption {

        /**
         * A p p_o n l y
         *
         * @constructor Create empty A p p_o n l y
         */
        APP_ONLY,

        /**
         * W h o l e_d e v i c e
         *
         * @constructor Create empty W h o l e_d e v i c e
         */
        WHOLE_DEVICE
    }

    override fun show(activity: androidx.fragment.app.FragmentActivity) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = PickerDialog()
        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    /**
     * Show the dialog
     *
     * @param activity launching
     * @param onChoose callback for the option chosen
     * @receiver
     */
    fun show(activity: androidx.fragment.app.FragmentActivity, onChoose: (sharingOption: SharingOption) -> Unit) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = PickerDialog().apply {
            this.onChoose = onChoose
        }
        show(activity)
    }

    internal class PickerDialog : KaleyraBottomSheetDialog() {

        override val isDraggable = true
        override val isDismissable = true

        var onChoose: ((sharingOption: SharingOption) -> Unit)? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_screenShareDialogStyle))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.kaleyra_screen_share_picker_dialog_layout, container).apply {
                findViewById<View>(R.id.kaleyra_app_screen_share)?.setOnClickListener {
                    onChoose?.invoke(SharingOption.APP_ONLY)
                    dismiss()
                }
                findViewById<View>(R.id.kaleyra_device_screen_share)?.setOnClickListener {
                    onChoose?.invoke(SharingOption.WHOLE_DEVICE)
                    dismiss()
                }
            }
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            onChoose = null
        }

        override fun onExpanded() = Unit

        override fun onCollapsed() = dismiss()

        override fun onDialogWillShow() = Unit

        override fun onSlide(offset: Float) = Unit

        override fun onStateChanged(newState: Int) {
            if (newState == KaleyraBottomSheetBehaviour.STATE_HIDDEN) dismiss()
        }
    }
}