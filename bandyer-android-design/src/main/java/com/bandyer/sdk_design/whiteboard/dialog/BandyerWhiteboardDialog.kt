package com.bandyer.sdk_design.whiteboard.dialog

import android.os.Bundle
import android.view.*
import android.webkit.*
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheetDialog
import com.bandyer.sdk_design.databinding.BandyerDialogWhiteboardBinding
import com.bandyer.sdk_design.dialogs.BandyerDialog
import com.bandyer.sdk_design.extensions.getCallThemeAttribute
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardLoadingError
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardUploadProgressLayout

abstract class BaseBandyerWhiteboardDialog<T : BaseBandyerWhiteboardDialog.BaseWhiteboardBottomSheetDialog>: BandyerDialog<T> {

    override val id: String = "bandyerWhiteBoardDialog"

    override var dialog: T? = null

    override fun show(activity: FragmentActivity) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = createDialog()
        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    abstract fun createDialog(session_id: String? = null) : T

    abstract class BaseWhiteboardBottomSheetDialog : BandyerBottomSheetDialog() {

        private var binding: BandyerDialogWhiteboardBinding? = null

        private var toolbar: Toolbar? = null

        var progressBar: ProgressBar? = null

        var loadingErrorLayout: BandyerWhiteboardLoadingError? = null

        var dialogLayout: CoordinatorLayout? = null

        var uploadProgressCard: BandyerWhiteboardUploadProgressLayout? = null

        var uploadButton: MenuItem? = null

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
            binding = BandyerDialogWhiteboardBinding.inflate(inflater, container, false)
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
                setNavigationOnClickListener { dismiss() }
            }
        }

        override fun onExpanded() = Unit

        override fun onCollapsed() = Unit

        override fun onDialogWillShow() = Unit

        override fun onSlide(offset: Float) = Unit

        override fun onStateChanged(newState: Int) = Unit

        abstract fun onReload()
    }
}