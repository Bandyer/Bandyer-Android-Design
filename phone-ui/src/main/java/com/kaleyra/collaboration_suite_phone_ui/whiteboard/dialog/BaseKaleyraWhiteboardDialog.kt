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

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.iterator
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheetDialog
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraWhiteboardDialogBinding
import com.kaleyra.collaboration_suite_phone_ui.dialogs.KaleyraDialog
import com.kaleyra.collaboration_suite_phone_ui.extensions.getCallThemeAttribute
import com.kaleyra.collaboration_suite_phone_ui.whiteboard.layout.KaleyraWhiteboardLoadingError
import com.kaleyra.collaboration_suite_phone_ui.whiteboard.layout.KaleyraWhiteboardUploadProgressLayout

/**
 * @suppress
 */
abstract class BaseKaleyraWhiteboardDialog<T : BaseKaleyraWhiteboardDialog.BaseWhiteboardBottomSheetDialog>: KaleyraDialog<T> {

    override val id: String = "KaleyraWhiteBoardDialog"

    override var dialog: T? = null

    override fun show(activity: FragmentActivity) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = createDialog()
        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    /**
     * Called to create a dialog
     * @param session_id the session id
     * @return T the initialized bottom sheet dialog
     */
    abstract fun createDialog(session_id: String? = null) : T

    /**
     * It initializes the UI of whiteboard bottom sheet dialog
     */
    abstract class BaseWhiteboardBottomSheetDialog : KaleyraBottomSheetDialog() {

        private var binding: KaleyraWhiteboardDialogBinding? = null

        private var toolbar: Toolbar? = null

        var progressBar: ProgressBar? = null

        var loadingErrorLayout: KaleyraWhiteboardLoadingError? = null

        var dialogLayout: CoordinatorLayout? = null

        var uploadProgressCard: KaleyraWhiteboardUploadProgressLayout? = null

        var uploadButton: MenuItem? = null
        var uploadButtonView: View? = null
        var backButtonView: View? = null

        var webViewStub: ViewStub? = null

        override fun onStart() {
            val parentLayout = dialog?.findViewById<ViewGroup>(R.id.design_bottom_sheet)
            val layoutParams = parentLayout?.layoutParams
            layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            parentLayout?.layoutParams = layoutParams
            super.onStart()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_whiteboardDialogStyle))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            binding = KaleyraWhiteboardDialogBinding.inflate(inflater, container, false)
            dialogLayout = binding!!.root
            toolbar = binding!!.kaleyraToolbar
            progressBar = binding!!.kaleyraProgressBar
            uploadProgressCard = binding!!.kaleyraUploadProgress
            loadingErrorLayout = binding!!.kaleyraLoadingError
            webViewStub = binding!!.kaleyraWebview
            loadingErrorLayout?.onReload(::onReload)
            return dialogLayout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            toolbar!!.apply {
                inflateMenu(R.menu.kaleyra_whiteboard_menu)
                uploadButton = menu.findItem(R.id.kaleyra_upload_file)
                uploadButtonView = view.findViewById(R.id.kaleyra_upload_file)
                backButtonView = iterator().next()
                setNavigationOnClickListener { dismiss() }
            }
        }

        override fun onExpanded() = Unit

        override fun onCollapsed() = Unit

        override fun onDialogWillShow() = Unit

        override fun onSlide(offset: Float) = Unit

        override fun onStateChanged(newState: Int) = Unit

        /**
         * Called when clicking the reload button of the loadingErrorLayout
         */
        abstract fun onReload()
    }
}