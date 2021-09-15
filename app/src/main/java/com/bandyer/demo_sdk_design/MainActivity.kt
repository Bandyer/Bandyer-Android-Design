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
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import com.bandyer.demo_sdk_design.databinding.ActivityMainBinding
import com.bandyer.demo_sdk_design.smartglass.SmartGlassActivity
import com.bandyer.video_android_phone_ui.bottom_sheet.items.ActionItem
import com.bandyer.video_android_phone_ui.call.bottom_sheet.items.CallAction
import com.bandyer.video_android_phone_ui.call.dialogs.BandyerSnapshotDialog
import com.bandyer.video_android_phone_ui.filesharing.BandyerFileShareDialog
import com.bandyer.video_android_phone_ui.filesharing.FileShareViewModel
import com.bandyer.video_android_phone_ui.filesharing.model.TransferData
import com.bandyer.video_android_phone_ui.smartglass.call.menu.SmartGlassActionItemMenu
import com.bandyer.video_android_phone_ui.smartglass.call.menu.SmartGlassMenuLayout
import com.bandyer.video_android_phone_ui.smartglass.call.menu.items.getSmartglassActions
import com.bandyer.video_android_phone_ui.smartglass.call.menu.utils.MotionEventInterceptor
import com.bandyer.video_android_phone_ui.whiteboard.dialog.BandyerWhiteboardTextEditorDialog
import com.bandyer.video_android_phone_ui.whiteboard.dialog.BandyerWhiteboardTextEditorDialog.BandyerWhiteboardTextEditorWidgetListener
import com.google.android.material.appbar.MaterialToolbar
import java.util.concurrent.ConcurrentHashMap

class MainActivity : AppCompatActivity() {

    var mText: String? = null

    private lateinit var binding: ActivityMainBinding

    private val viewModel: LocalFileShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        initializeListeners()
    }

    private fun initializeListeners() = with(binding) {
        btnChat.setOnClickListener { startActivity(Intent(this@MainActivity, ChatActivity::class.java)) }

        btnCall.setOnClickListener { startActivity(Intent(this@MainActivity, CallActivity::class.java)) }

        btnSmartglassesMenu.setOnClickListener { showSmartGlassAction() }

        btnWhiteboard.setOnClickListener { WhiteBoardDialog().show(this@MainActivity) }

        btnRinging.setOnClickListener { startActivity(Intent(this@MainActivity, RingingActivity::class.java)) }

        btnSwitchNightMode.setOnClickListener {
            val isNightTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (isNightTheme) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    window.setWindowAnimations(R.style.Bandyer_ThemeTransitionAnimation)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                Configuration.UI_MODE_NIGHT_NO  -> {
                    window.setWindowAnimations(R.style.Bandyer_ThemeTransitionAnimation)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }

        btnWhiteboardEditor.setOnClickListener {
            BandyerWhiteboardTextEditorDialog().show(this@MainActivity, mText, object : BandyerWhiteboardTextEditorWidgetListener {
                override fun onTextEditConfirmed(newText: String) {
                    mText = newText
                }
            })
        }

        btnSnapshotPreview.setOnClickListener {
            BandyerSnapshotDialog().show(this@MainActivity)
        }

        btnLivePointer.setOnClickListener { startActivity(Intent(this@MainActivity, PointerActivity::class.java)) }

        btnBluetoothAudioroute.setOnClickListener { startActivity(Intent(this@MainActivity, BluetoothAudioRouteActivity::class.java)) }

        btnFileShare.setOnClickListener {
            val fileShareDialog = BandyerFileShareDialog()
            fileShareDialog.show(this@MainActivity, viewModel) {}
            fileShareDialog.dialog?.view?.findViewById<View>(R.id.bandyer_upload_file_fab)?.setOnClickListener {
                viewModel.itemsData["id_1"] = TransferData(this@MainActivity,"1", "".toUri(), "razer.jpg", "image/jpeg","Gianluigi", size = 100L, state = TransferData.State.Available, type = TransferData.Type.Download)
                viewModel.itemsData["id_2"] = TransferData(this@MainActivity,"2", "".toUri(), "identity_card.pdf", "","Mario", bytesTransferred = 100L, size = 100L, state = TransferData.State.Success, type = TransferData.Type.Download)
                viewModel.itemsData["id_3"] = TransferData(this@MainActivity,"3", "".toUri(), "car.zip", "application/zip","Luigi", bytesTransferred = 600L, size = 1000L, state = TransferData.State.OnProgress, type = TransferData.Type.Download)
                viewModel.itemsData["id_4"] = TransferData(this@MainActivity,"4", "".toUri(), "phone.doc", "","Gianni", size = 23000000L, state = TransferData.State.Error, type = TransferData.Type.Upload)
                viewModel.itemsData["id_5"] = TransferData(this@MainActivity,"5", "".toUri(), "address.jpg", "image/jpeg","Marco", size = 1000L, state = TransferData.State.Pending, type = TransferData.Type.Upload)
                fileShareDialog.notifyDataSetChanged()
            }
            fileShareDialog.setOnDismissListener { viewModel.itemsData.clear() }
        }

        btnSmartglass.setOnClickListener { startActivity(Intent(this@MainActivity, SmartGlassActivity::class.java)) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    private fun showSmartGlassAction(): SmartGlassActionItemMenu = SmartGlassActionItemMenu.show(
        appCompatActivity = this,
        items = CallAction.getSmartglassActions(
            ctx = this,
            micToggled = false,
            cameraToggled = false
        )
    )
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

class LocalFileShareViewModel : FileShareViewModel() {

    override val itemsData: MutableMap<String, TransferData> = ConcurrentHashMap()

    override fun uploadFile(context: Context, id: String, uri: Uri, sender: String) = Unit

    override fun downloadFile(context: Context, id: String, uri: Uri, sender: String) = Unit

    override fun cancelFileUpload(uploadId: String) = Unit

    override fun cancelFileDownload(downloadId: String) = Unit

    override fun cancelAllFileUploads() = Unit

    override fun cancelAllFileDownloads() = Unit
}
