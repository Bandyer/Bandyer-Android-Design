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

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import com.bandyer.demo_sdk_design.databinding.ActivityMainBinding
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.call.dialogs.BandyerSnapshotDialog
import com.bandyer.sdk_design.filesharing.BandyerFileShareDialog
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import com.bandyer.sdk_design.filesharing.model.FileData
import com.bandyer.sdk_design.filesharing.model.TransferData
import com.bandyer.sdk_design.smartglass.call.menu.SmartGlassActionItemMenu
import com.bandyer.sdk_design.smartglass.call.menu.SmartGlassMenuLayout
import com.bandyer.sdk_design.smartglass.call.menu.items.getSmartglassActions
import com.bandyer.sdk_design.smartglass.call.menu.utils.MotionEventInterceptor
import com.bandyer.sdk_design.whiteboard.dialog.BandyerWhiteboardTextEditorDialog
import com.bandyer.sdk_design.whiteboard.dialog.BandyerWhiteboardTextEditorDialog.BandyerWhiteboardTextEditorWidgetListener
import com.google.android.material.appbar.MaterialToolbar
import java.util.concurrent.ConcurrentHashMap

class MainActivity : AppCompatActivity() {

    companion object {
        const val tag = "MainAcitivy"
    }

    var mText: String? = null

    private lateinit var binding: ActivityMainBinding

    private val viewModel: LocalFileShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        initializeListeners()
        initFileShareItems()
    }

    private fun initFileShareItems() {
        viewModel.itemsData["id_1"] = TransferData(FileData(id = "1", uri = "".toUri(), name = "razer.jpg", mimeType = "image/jpeg", sender = "Gianluigi", size = 100L), TransferData.State.Pending, TransferData.Type.DownloadAvailable)
        viewModel.itemsData["id_2"] = TransferData(FileData(id = "2", uri = "".toUri(), name = "identity_card.pdf", mimeType = "", sender = "Mario", size = 100L), TransferData.State.Success("".toUri()), TransferData.Type.Download)
        viewModel.itemsData["id_3"] = TransferData(FileData(id = "3", uri = "".toUri(), name = "car.zip", mimeType = "application/zip", sender = "Luigi", size = 1000L), TransferData.State.OnProgress(600L), TransferData.Type.Download)
        viewModel.itemsData["id_4"] = TransferData(FileData(id = "4", uri = "".toUri(), name = "phone.doc", mimeType = "", sender = "Gianni", size = 23000000L), TransferData.State.Error(Throwable()), TransferData.Type.Upload)
        viewModel.itemsData["id_5"] = TransferData(FileData(id = "5", uri = "".toUri(), name = "address.jpg", mimeType = "image/jpeg", sender = "Marco", size = 1000L), TransferData.State.Pending, TransferData.Type.Upload)
    }

    private fun initializeListeners() {
        binding.btnChat.setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }

        binding.btnCall.setOnClickListener { startActivity(Intent(this, CallActivity::class.java)) }

        binding.btnSmartglassesMenu.setOnClickListener { showSmartGlassAction() }

        binding.btnWhiteboard.setOnClickListener { WhiteBoardDialog().show(this@MainActivity) }

        binding.btnRinging.setOnClickListener { startActivity(Intent(this, RingingActivity::class.java)) }

        binding.btnSwitchNightMode.setOnClickListener {
            val isNightTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (isNightTheme) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    window.setWindowAnimations(R.style.Bandyer_ThemeTransitionAnimation)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    window.setWindowAnimations(R.style.Bandyer_ThemeTransitionAnimation)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }

        binding.btnWhiteboardEditor.setOnClickListener {
            BandyerWhiteboardTextEditorDialog().show(this@MainActivity, mText, object : BandyerWhiteboardTextEditorWidgetListener {
                override fun onTextEditConfirmed(newText: String) {
                    mText = newText
                }
            })
        }

        binding.btnSnapshotPreview.setOnClickListener {
            BandyerSnapshotDialog().show(this@MainActivity)
        }

        binding.btnLivePointer.setOnClickListener { startActivity(Intent(this, PointerActivity::class.java)) }

        binding.btnBluetoothAudioroute.setOnClickListener { startActivity(Intent(this, BluetoothAudioRouteActivity::class.java)) }

        binding.btnFileShare.setOnClickListener { BandyerFileShareDialog().show(this@MainActivity, viewModel, viewModel.itemsData) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    private fun showSmartGlassAction(): SmartGlassActionItemMenu = SmartGlassActionItemMenu.show(
            appCompatActivity = this,
            items = CallAction.getSmartglassActions(
                    ctx = this,
                    micToggled = false,
                    cameraToggled = false))
            .apply {
                selectionListener = object : SmartGlassMenuLayout.OnSmartglassMenuSelectionListener {
                    override fun onSelected(item: ActionItem) {
                        Toast.makeText(applicationContext, item::class.java.simpleName, Toast.LENGTH_SHORT).show()
                        dismiss()
                        selectionListener = null
                    }

                    override fun onDismiss() = Unit
                }
                motionEventInterceptor = object : MotionEventInterceptor {
                    override fun onMotionEventIntercepted(event: MotionEvent?) {
                        Log.d(tag, "$event")
                    }
                }
            }
}

class LocalFileShareViewModel: FileShareViewModel() {
    val itemsData: ConcurrentHashMap<String, TransferData> = ConcurrentHashMap()

    override fun uploadFile(context: Context, id: String, uri: Uri, sender: String) = Unit

    override fun downloadFile(context: Context, id: String, uri: Uri, sender: String) = Unit

    override fun cancelFileUpload(uploadId: String) = Unit

    override fun cancelFileDownload(downloadId: String) = Unit

    override fun cancelAllFileUploads() = Unit

    override fun cancelAllFileDownloads() = Unit
}
