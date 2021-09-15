package com.bandyer.demo_sdk_design

import android.os.Bundle
import android.view.View
import android.webkit.*
import com.bandyer.video_android_phone_ui.extensions.replaceWith
import com.bandyer.video_android_phone_ui.whiteboard.dialog.BaseBandyerWhiteboardDialog

class WhiteBoardDialog : BaseBandyerWhiteboardDialog<WhiteBoardDialog.WhiteboardBottomSheetDialog>() {

    override val id: String = "bandyerWhiteBoardDialog"

    override var dialog: WhiteboardBottomSheetDialog? = null

    override fun createDialog(session_id: String?): WhiteboardBottomSheetDialog = WhiteboardBottomSheetDialog().apply {
        if(session_id == null) return@apply
    }

    class WhiteboardBottomSheetDialog : BaseWhiteboardBottomSheetDialog() {

        var webView: WebView? = null

        var wvClient: WebViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView!!.loadUrl("javascript:init(\"\",\"\")")
                progressBar?.visibility = View.GONE
                webView!!.visibility = View.VISIBLE
                initProgressCard()
                super.onPageFinished(view, url)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            progressBar?.visibility = View.GONE
        }

        private fun initProgressCard() {
            uploadProgressCard!!.showUploading("Uploading file", "compressing..", 60f)
            uploadProgressCard!!.setOnClickListener { uploadProgressCard!!.showError("Error", "Something went wrong") }
        }

        private fun initWebView() {
            webView = WebView(requireContext()).apply {
                setInitialScale(1)

                settings.apply {
                    setAppCacheEnabled(true)
                    domStorageEnabled = true
                    databaseEnabled = true
                    displayZoomControls = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    loadWithOverviewMode = true
                    setAppCachePath(context.cacheDir.path)
                    allowFileAccess = true
                    cacheMode = WebSettings.LOAD_NO_CACHE // load online by default
                    javaScriptEnabled = true
                    useWideViewPort = true
                    setSupportZoom(false)
                }

                webViewClient = wvClient
            }
        }

        override fun onReload() {
            initWebView()
            webViewStub?.replaceWith(webView!!)
            webView!!.visibility = View.GONE
            progressBar!!.visibility = View.VISIBLE
            loadingErrorLayout!!.let { it.postDelayed({ it.visibility = View.GONE }, 300) }
            webView!!.loadUrl("https://sandbox.bandyer.com/connect/mobile-whiteboard")
        }

    }
}
