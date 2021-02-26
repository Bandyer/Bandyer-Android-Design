package com.bandyer.demo_sdk_design

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.*
import androidx.core.os.HandlerCompat.postDelayed
import com.bandyer.sdk_design.extensions.replaceWith
import com.bandyer.sdk_design.whiteboard.dialog.BaseBandyerWhiteboardDialog
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardUploadProgressLayout

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
                progressBar?.visibility = View.GONE
                webView!!.loadUrl("javascript:init(\"\",\"\")")
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
            uploadProgressCard!!.setOnClickListener {
                uploadProgressCard!!.showError("Error", "Something went wrong") }
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

            webViewStub?.replaceWith(webView!!)

            loadingErrorLayout?.let { it.postDelayed({ it.visibility = View.GONE }, 300) }
        }

        override fun onReload() {
            initWebView()
            webView!!.loadUrl("https://sandbox.bandyer.com/connect/mobile-whiteboard")
        }
    }
}
