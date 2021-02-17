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

package com.bandyer.demo_sdk_design

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardLoadingError
import com.bandyer.sdk_design.whiteboard.layout.BandyerWhiteboardUploadProgressLayout
import com.google.android.material.appbar.MaterialToolbar

class WhiteboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bandyer_dialog_whiteboard)

        initializeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initializeUi() {
        initToolbar()
        initProgressCard()
        initLoadingError()
    }

    private fun initProgressCard() {
        val progress = findViewById<BandyerWhiteboardUploadProgressLayout>(R.id.upload_progress)
        progress.progressBar!!.setProgressCompat(60, true)
        progress.progressText!!.text = "60%"
        progress.progressTitle!!.text = "Uploading file"
        progress.progressSubtitle!!.text = "compressing.."
        progress.state = BandyerWhiteboardUploadProgressLayout.State.UPLOADING
        progress.setOnClickListener {
            progress.state = when(progress.state) {
                BandyerWhiteboardUploadProgressLayout.State.UPLOADING -> BandyerWhiteboardUploadProgressLayout.State.ERROR
                else -> BandyerWhiteboardUploadProgressLayout.State.UPLOADING
            }
        }
    }

    private fun initToolbar() {
        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            inflateMenu(R.menu.whiteboard_menu)
            val uploadButton = menu.findItem(R.id.upload_file)
            setOnMenuItemClickListener {
                uploadButton.isEnabled = false
                true
            }
            setNavigationOnClickListener { finish() }
        }
    }

    private fun initLoadingError() {
        findViewById<BandyerWhiteboardLoadingError>(R.id.loading_error).apply {
            this.onReload {  }
        }
    }
}