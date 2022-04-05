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

package com.kaleyra.collaboration_suite_phone_ui.whiteboard.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment

import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingLeft
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingRight
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingTop
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheetDialog
import com.kaleyra.collaboration_suite_phone_ui.dialogs.KaleyraDialog
import com.kaleyra.collaboration_suite_phone_ui.extensions.*
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutObserver
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutOffsetListener
import com.kaleyra.collaboration_suite_phone_ui.whiteboard.dialog.KaleyraWhiteboardTextEditorDialog.CustomTextEditorDialog.Companion.TEXT_PLACEHOLDER
import com.kaleyra.collaboration_suite_phone_ui.whiteboard.layout.KaleyraWhiteboardTextEditorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED


/**
 * @suppress
 */
class KaleyraWhiteboardTextEditorDialog : KaleyraDialog<KaleyraWhiteboardTextEditorDialog.CustomTextEditorDialog> {

    override var dialog: CustomTextEditorDialog? = null

    override val id: String = "KaleyraWhiteboardTextEditorDialog"

    override fun show(activity: androidx.fragment.app.FragmentActivity) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = CustomTextEditorDialog()
        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    fun show(activity: androidx.fragment.app.FragmentActivity, oldText: String? = null, listener: KaleyraWhiteboardTextEditorWidgetListener): KaleyraWhiteboardTextEditorDialog {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return this
        if (dialog == null) dialog = CustomTextEditorDialog()
        dialog!!.setListener(listener)
        dialog!!.arguments = Bundle()
        dialog!!.requireArguments().putString(TEXT_PLACEHOLDER, oldText)
        show(activity)
        return this
    }

    /**
     * @suppress
     */
    interface KaleyraWhiteboardTextEditorWidgetListener {
        fun onTextEditConfirmed(newText: String)
    }

    class CustomTextEditorDialog : KaleyraBottomSheetDialog(), TextWatcher, SystemViewLayoutObserver {

        companion object {
            const val TEXT_PLACEHOLDER = "text_placeholder"
        }

        private var mText: String? = null

        private var callback: KaleyraWhiteboardTextEditorWidgetListener? = null
        private val collapsed: Boolean
            get() = behavior?.state == STATE_COLLAPSED

        private var whiteboardTextEditorLayout: KaleyraWhiteboardTextEditorLayout? = null

        private var marginTop = 0

        fun setListener(listener: KaleyraWhiteboardTextEditorWidgetListener) {
            callback = listener
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mText = arguments?.getString(TEXT_PLACEHOLDER)
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getWhiteboardDialogAttribute(R.styleable.KaleyraCollaborationSuiteUI_BottomSheetDialog_Whiteboard_kaleyra_textEditorDialogStyle))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            whiteboardTextEditorLayout = KaleyraWhiteboardTextEditorLayout(ContextThemeWrapper(requireContext(), requireContext().getTextEditorDialogAttribute(R.styleable.KaleyraCollaborationSuiteUI_BottomSheetDialog_TextEditor_kaleyra_textEditorStyle)))
            return whiteboardTextEditorLayout
        }

        private fun setupText(text: String?) {
            if (text.isNullOrBlank()) whiteboardTextEditorLayout?.showKeyboard()
            else {
                val editText = whiteboardTextEditorLayout?.inputTextField
                editText?.setText(text)
                editText?.setSelection(text.length)
            }
        }

        override fun onDialogWillShow() {
            setupText(mText)
            whiteboardTextEditorLayout?.acceptInputButton?.setOnClickListener {
                val text = whiteboardTextEditorLayout!!.text
                if (!collapsed && textHasChanged()) callback?.onTextEditConfirmed(text)
                dismiss()
            }

            whiteboardTextEditorLayout?.cancelInputButton?.setOnClickListener {
                when {
                    collapsed -> expand()
                    !textHasChanged() -> dismiss()
                    else -> collapse()
                }
            }
            whiteboardTextEditorLayout?.inputTextField?.addTextChangedListener(this)
        }

        override fun afterTextChanged(s: Editable?) {
            val textHasChanged = textHasChanged()
            whiteboardTextEditorLayout?.setTextAsChanged(textHasChanged)
            setCollapsedHeight()
        }

        private fun setCollapsedHeight() {
            whiteboardTextEditorLayout ?: return
            context ?: return
            behavior ?: return
            val peek = whiteboardTextEditorLayout!!.getDiscardChangesModeHeight()
            if (textHasChanged()) behavior!!.peekHeight = peek
            else behavior!!.peekHeight = 0
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun onSlide(offset: Float) {
            val alpha = if (collapsed) 1f - offset else offset
            whiteboardTextEditorLayout?.alphaAnimateTextArea(alpha)
        }

        override fun onStateChanged(@BottomSheetBehavior.State newState: Int) {

        }

        override fun dismiss() {
            whiteboardTextEditorLayout?.inputTextField?.isCursorVisible = false
            whiteboardTextEditorLayout?.inputTextField?.text = null
            whiteboardTextEditorLayout?.inputTextField?.removeTextChangedListener(this)
            whiteboardTextEditorLayout?.hideKeyboard(true)
            callback = null
            super.dismiss()
        }

        override fun onDismiss(dialog: DialogInterface) {
            whiteboardTextEditorLayout?.hideKeyboard(true)
            whiteboardTextEditorLayout?.inputTextField?.text = null
            callback = null
            super.onDismiss(dialog)
        }

        override fun onResume() {
            super.onResume()

            requireActivity().scanForFragmentActivity()?.let {
                SystemViewLayoutOffsetListener.addObserver(it, this)
            }

            dialog?.setOnKeyListener { _, keyCode, event ->
                val isBack = keyCode == KeyEvent.KEYCODE_BACK
                if (isBack && event!!.action == KeyEvent.ACTION_UP && !collapsed) {
                    val newText = whiteboardTextEditorLayout!!.text
                    if (mText != newText) collapse()
                    else dismiss()
                }
                isBack
            }
        }

        override fun onPause() {
            super.onPause()
            requireActivity().scanForFragmentActivity()?.let {
                SystemViewLayoutOffsetListener.removeObserver(it as AppCompatActivity, this)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            dismiss()
        }

        override fun onTopInsetChanged(pixels: Int) {
            marginTop = pixels
            whiteboardTextEditorLayout?.dataLossMessage?.setPaddingTop(pixels)
        }

        override fun onBottomInsetChanged(pixels: Int) {
            whiteboardTextEditorLayout ?: return
            if (pixels in 0..200) setCollapsedHeight()
        }

        override fun onRightInsetChanged(pixels: Int) {
            whiteboardTextEditorLayout?.setPaddingRight(pixels)
        }

        override fun onLeftInsetChanged(pixels: Int) {
            whiteboardTextEditorLayout?.setPaddingLeft(pixels)
        }

        override fun onCollapsed() {
            if (!textHasChanged()) dismiss()
            else whiteboardTextEditorLayout?.showDiscardChangesMode()
        }

        override fun onExpanded() {
            whiteboardTextEditorLayout?.showEditMode(textHasChanged())
        }

        private fun textHasChanged(): Boolean {
            val text = whiteboardTextEditorLayout?.text
            return (mText == null && !text.isNullOrBlank()) || (mText != null && mText != text)
        }
    }
}