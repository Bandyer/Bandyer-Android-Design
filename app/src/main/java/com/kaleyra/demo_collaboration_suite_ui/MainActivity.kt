/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.demo_collaboration_suite_ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.kaleyra.collaboration_suite_core_ui.notification.ChatNotificationManager
import com.kaleyra.collaboration_suite_core_ui.notification.ChatNotificationMessage
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items.CallAction
import com.kaleyra.collaboration_suite_phone_ui.feedback.FeedbackDialog
import com.kaleyra.collaboration_suite_phone_ui.filesharing.FileShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.filesharing.KaleyraFileShareDialog
import com.kaleyra.collaboration_suite_phone_ui.filesharing.model.TransferData
import com.kaleyra.collaboration_suite_phone_ui.recording.KaleyraRecordingSnackbar
import com.kaleyra.collaboration_suite_phone_ui.smartglass.call.menu.SmartGlassActionItemMenu
import com.kaleyra.collaboration_suite_phone_ui.smartglass.call.menu.SmartGlassMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.smartglass.call.menu.items.getSmartglassActions
import com.kaleyra.collaboration_suite_phone_ui.smartglass.call.menu.utils.MotionEventInterceptor
import com.kaleyra.collaboration_suite_phone_ui.whiteboard.dialog.KaleyraWhiteboardTextEditorDialog
import com.kaleyra.demo_collaboration_suite_ui.databinding.ActivityMainBinding
import java.util.concurrent.ConcurrentHashMap


class MainActivity : AppCompatActivity(), ChatNotificationManager {

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
        btnKaleyraSnackbar.setOnClickListener {
            KaleyraRecordingSnackbar.make(
                binding.root,
                KaleyraRecordingSnackbar.Type.TYPE_ENDED,
                LENGTH_LONG
            ).show()
        }

        btnChat.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                val notification = buildChatNotification(
                    "Stefano",
                    "Stefano",
                    Uri.EMPTY,
                    "chatId",
                    listOf(
                        ChatNotificationMessage(
                            "Mario",
                            "Mario",
                            Uri.EMPTY,
                            "Ciao belli",
                            1
                        ),
                        ChatNotificationMessage (
                            "Gioele",
                            "Gioele",
                            Uri.EMPTY,
                            "Andiamo in montagna",
                            2
                        ),
                        ChatNotificationMessage(
                            "Omar",
                            "Omar",
                            Uri.EMPTY,
                            "La pizza è buona?",
                            3
                        )
                    ),
                    ChatActivity::class.java,
                    false
                )

                NotificationManagerCompat.from(this@MainActivity).notify(67, notification)
            }, 3000)

//            startActivity(Intent(this@MainActivity, ChatActivity::class.java))
        }

        btnCall.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    CallActivity::class.java
                )
            )
        }

        btnSmartglassesMenu.setOnClickListener { showSmartGlassAction() }

        btnWhiteboard.setOnClickListener { WhiteBoardDialog().show(this@MainActivity) }

        btnRinging.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    RingingActivity::class.java
                )
            )
        }

        btnSwitchNightMode.setOnClickListener {
            val isNightTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (isNightTheme) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    window.setWindowAnimations(R.style.Kaleyra_ThemeTransitionAnimation)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    window.setWindowAnimations(R.style.Kaleyra_ThemeTransitionAnimation)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }

        btnWhiteboardEditor.setOnClickListener {
            KaleyraWhiteboardTextEditorDialog().show(this@MainActivity, mText, object :
                KaleyraWhiteboardTextEditorDialog.KaleyraWhiteboardTextEditorWidgetListener {
                override fun onTextEditConfirmed(newText: String) {
                    mText = newText
                }
            })
        }

        btnLivePointer.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    PointerActivity::class.java
                )
            )
        }

        btnBluetoothAudioroute.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    BluetoothAudioRouteActivity::class.java
                )
            )
        }

        btnFileShare.setOnClickListener {
            val fileShareDialog = KaleyraFileShareDialog()
            fileShareDialog.show(this@MainActivity, viewModel) {}
            fileShareDialog.dialog?.view?.findViewById<View>(R.id.kaleyra_upload_file_fab)
                ?.setOnClickListener {
                    viewModel.itemsData["id_1"] = TransferData(
                        this@MainActivity,
                        "1",
                        "".toUri(),
                        "razer.jpg",
                        "image/jpeg",
                        "Gianluigi",
                        size = 100L,
                        state = TransferData.State.Available,
                        type = TransferData.Type.Download
                    )
                    viewModel.itemsData["id_2"] = TransferData(
                        this@MainActivity,
                        "2",
                        "".toUri(),
                        "identity_card.pdf",
                        "",
                        "Mario",
                        bytesTransferred = 100L,
                        size = 100L,
                        state = TransferData.State.Success,
                        type = TransferData.Type.Download
                    )
                    viewModel.itemsData["id_3"] = TransferData(
                        this@MainActivity,
                        "3",
                        "".toUri(),
                        "car.zip",
                        "application/zip",
                        "Luigi",
                        bytesTransferred = 600L,
                        size = 1000L,
                        state = TransferData.State.OnProgress,
                        type = TransferData.Type.Download
                    )
                    viewModel.itemsData["id_4"] = TransferData(
                        this@MainActivity,
                        "4",
                        "".toUri(),
                        "phone.doc",
                        "",
                        "Gianni",
                        size = 23000000L,
                        state = TransferData.State.Error,
                        type = TransferData.Type.Upload
                    )
                    viewModel.itemsData["id_5"] = TransferData(
                        this@MainActivity,
                        "5",
                        "".toUri(),
                        "address.jpg",
                        "image/jpeg",
                        "Marco",
                        size = 1000L,
                        state = TransferData.State.Pending,
                        type = TransferData.Type.Upload
                    )
                    fileShareDialog.notifyDataSetChanged()
                }
            fileShareDialog.setOnDismissListener { viewModel.itemsData.clear() }
        }

        btnFeedback.setOnClickListener {
            FeedbackDialog().show(
                supportFragmentManager,
                FeedbackDialog.TAG
            )
        }

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
                    Toast.makeText(
                        applicationContext,
                        item::class.java.simpleName,
                        Toast.LENGTH_SHORT
                    ).show()
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
