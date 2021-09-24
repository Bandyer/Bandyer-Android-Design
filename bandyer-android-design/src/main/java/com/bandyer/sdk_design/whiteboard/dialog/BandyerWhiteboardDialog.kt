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

package com.bandyer.sdk_design.whiteboard.dialog

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.iterator
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheetDialog
import com.bandyer.sdk_design.databinding.BandyerWhiteboardDialogBinding
import com.bandyer.sdk_design.dialogs.BandyerDialog
import com.bandyer.sdk_design.extensions.getCallThemeAttribute
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardLoadingError
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardUploadProgressLayout

/**
 * @suppress
 */
abstract class BaseBandyerWhiteboardDialog<T : BaseBandyerWhiteboardDialog.BaseWhiteboardBottomSheetDialog>: BandyerDialog<T> {

    override val id: String = "bandyerWhiteBoardDialog"

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
    abstract class BaseWhiteboardBottomSheetDialog : BandyerBottomSheetDialog() {

        private var binding: BandyerWhiteboardDialogBinding? = null

        private var toolbar: Toolbar? = null

        var progressBar: ProgressBar? = null

        var loadingErrorLayout: BandyerWhiteboardLoadingError? = null

        var dialogLayout: CoordinatorLayout? = null

        var uploadProgressCard: BandyerWhiteboardUploadProgressLayout? = null

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
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_whiteboardDialogStyle))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            binding = BandyerWhiteboardDialogBinding.inflate(inflater, container, false)
            dialogLayout = binding!!.root
            toolbar = binding!!.bandyerToolbar
            progressBar = binding!!.bandyerProgressBar
            uploadProgressCard = binding!!.bandyerUploadProgress
            loadingErrorLayout = binding!!.bandyerLoadingError
            webViewStub = binding!!.bandyerWebview
            loadingErrorLayout?.onReload(::onReload)
            return dialogLayout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            toolbar!!.apply {
                inflateMenu(R.menu.bandyer_whiteboard_menu)
                uploadButton = menu.findItem(R.id.bandyer_upload_file)
                uploadButtonView = view.findViewById(R.id.bandyer_upload_file)
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