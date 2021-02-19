package com.bandyer.sdk_design.whiteboard.dialog

import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.webkit.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheetDialog
import com.bandyer.sdk_design.databinding.BandyerActionButtonAudiorouteBinding
import com.bandyer.sdk_design.databinding.BandyerDialogWhiteboardBinding
import com.bandyer.sdk_design.dialogs.BandyerDialog
import com.bandyer.sdk_design.extensions.getCallThemeAttribute
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardLoadingError
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardUploadProgressLayout

abstract class BaseBandyerWhiteboardDialog<T: BaseBandyerWhiteboardDialog.BaseWhiteboardBottomSheetDialog>: BandyerDialog<T> {

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

        private var toolbar: Toolbar? = null

        var progressBar: ProgressBar? = null

        var loadingErrorLayout: BandyerWhiteboardLoadingError? = null

        var dialogLayout: CoordinatorLayout? = null

        var uploadProgressCard: BandyerWhiteboardUploadProgressLayout? = null

        var uploadButton: MenuItem? = null

        var webViewStub: ViewStub? = null

        var mWebViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar?.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                progressBar?.visibility = View.GONE
                loadingErrorLayout?.visibility = View.VISIBLE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingErrorLayout?.visibility = View.GONE
                progressBar?.visibility = View.VISIBLE
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
//            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_whiteboardDialogStyle))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            dialogLayout = inflater.inflate(R.layout.bandyer_dialog_whiteboard, container) as CoordinatorLayout
            toolbar = dialogLayout!!.findViewById(R.id.bandyer_toolbar)
            progressBar = dialogLayout!!.findViewById(R.id.bandyer_progress_bar)
            uploadProgressCard = dialogLayout!!.findViewById(R.id.bandyer_upload_progress)
            loadingErrorLayout = dialogLayout!!.findViewById(R.id.bandyer_loading_error)
            webViewStub = dialogLayout!!.findViewById(R.id.bandyer_webview)
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