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

package com.kaleyra.collaboration_suite_phone_ui.virtualbackground

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
import com.kaleyra.collaboration_suite_phone_ui.screensharing.dialog.KaleyraScreenSharePickerDialog

/**
 * Kaleyra virtual background picker dialog
 *
 * @constructor Create empty Kaleyra virtual background picker dialog
 */
class KaleyraVirtualBackgroundPickerDialog(private val withBlurVirtualBackgroundOption: Boolean = true, private val withImageVirtualBackground: Boolean = true): KaleyraDialog<KaleyraBottomSheetDialog> {

    override var dialog: KaleyraBottomSheetDialog? = null

    override val id: String = "KaleyraVirtualBackgroundPickerDialog"

    /**
     * Virtual Background option
     *
     * @constructor Create empty Virtual Background Option
     */
    enum class VirtualBackgroundOptions {


        /**
         * No virtual background selected
         */
        NONE,

        /**
         * Blur virtual background
         */
        BLUR,

        /**
         * Image virtual background
         */
        IMAGE
    }

    override fun show(activity: androidx.fragment.app.FragmentActivity) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = PickerDialog(withBlurVirtualBackgroundOption, withImageVirtualBackground)
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
    fun show(activity: androidx.fragment.app.FragmentActivity, onChoose: (virtualBackgroundOption: VirtualBackgroundOptions) -> Unit) = kotlin.runCatching {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return@runCatching
        if (dialog == null) dialog = PickerDialog(withBlurVirtualBackgroundOption, withBlurVirtualBackgroundOption).apply {
            this.onChoose = onChoose
        }
        show(activity)
    }

    internal class PickerDialog(private val withBlurVirtualBackgroundOption: Boolean = true, private val withVirtualImageBackgroundOption: Boolean) : KaleyraBottomSheetDialog() {

        override val isDraggable = true
        override val isDismissable = true

        var onChoose: ((sharingOption: VirtualBackgroundOptions) -> Unit)? = null

        lateinit var blurVirtualBackgroundButton: View
            private set

        lateinit var imageVirtualBackgroundButton: View
            private set

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_virtualBackgroundDialogStyle))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.kaleyra_virtual_background_picker_dialog_layout, container).apply {
                findViewById<View>(R.id.kaleyra_none_virtual_background)?.setOnClickListener {
                    onChoose?.invoke(VirtualBackgroundOptions.NONE)
                    dismiss()
                }
                blurVirtualBackgroundButton = findViewById(R.id.kaleyra_blur_virtual_background)!!
                blurVirtualBackgroundButton.setOnClickListener {
                    onChoose?.invoke(VirtualBackgroundOptions.BLUR)
                    dismiss()
                }
                imageVirtualBackgroundButton = findViewById(R.id.kaleyra_image_virtual_background)!!
                imageVirtualBackgroundButton.setOnClickListener {
                    onChoose?.invoke(VirtualBackgroundOptions.IMAGE)
                    dismiss()
                }
                if (!withBlurVirtualBackgroundOption) blurVirtualBackgroundButton.visibility = View.GONE
                if (!withVirtualImageBackgroundOption) imageVirtualBackgroundButton.visibility = View.GONE
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