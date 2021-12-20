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

package com.bandyer.app_design

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
