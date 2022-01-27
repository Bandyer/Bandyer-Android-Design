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

import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.bandyer.app_design.databinding.ActivityMainBinding
import com.bandyer.video_android_phone_ui.bottom_sheet.items.ActionItem
import com.bandyer.video_android_phone_ui.call.bottom_sheet.items.CallAction
import com.bandyer.video_android_phone_ui.feedback.FeedbackDialog
import com.bandyer.video_android_phone_ui.filesharing.BandyerFileShareDialog
import com.bandyer.video_android_phone_ui.filesharing.FileShareViewModel
import com.bandyer.video_android_phone_ui.filesharing.model.TransferData
import com.bandyer.video_android_phone_ui.smartglass.call.menu.SmartGlassActionItemMenu
import com.bandyer.video_android_phone_ui.smartglass.call.menu.SmartGlassMenuLayout
import com.bandyer.video_android_phone_ui.smartglass.call.menu.items.getSmartglassActions
import com.bandyer.video_android_phone_ui.smartglass.call.menu.utils.MotionEventInterceptor
import com.bandyer.video_android_phone_ui.whiteboard.dialog.BandyerWhiteboardTextEditorDialog
import com.google.android.material.appbar.MaterialToolbar
import com.squareup.picasso.Picasso
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
            BandyerWhiteboardTextEditorDialog().show(this@MainActivity, mText, object :
                BandyerWhiteboardTextEditorDialog.BandyerWhiteboardTextEditorWidgetListener {
                override fun onTextEditConfirmed(newText: String) {
                    mText = newText
                }
            })
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

        btnFeedback.setOnClickListener { FeedbackDialog().show(supportFragmentManager, FeedbackDialog.TAG) }

        btnNotification.setOnClickListener { startActivity(Intent(this@MainActivity, NotificationActivity::class.java)) }
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

    private fun initNotification() {
        val callIntent = Intent(this, CallActivity::class.java)
        val ringingIntent = Intent(this, RingingActivity::class.java)

        TaskStackBuilder.create(this).apply {
            addParentStack(CallActivity::class.java)
            addNextIntent(callIntent)
        }

        val callPendingIntent = PendingIntent.getActivity(
            this,
            0,
            callIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val name = "Mario"
        val subText = "Sottotitolo"
        val avatar = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_bandyer_avatar_bold
        )
        val customView = RemoteViews(packageName, R.layout.bandyer_notification2)
        customView.setTextViewText(R.id.name, name)
//        customView.setViewVisibility(R.id.subtitle, View.GONE)
        customView.setTextViewText(R.id.title, subText)

        customView.setTextViewText(
            R.id.answer_text,
           "Answer"
        )
        customView.setTextViewText(
            R.id.decline_text,
            "Decline"
        )
        customView.setImageViewBitmap(R.id.photo, avatar)
//        customView.setOnClickPendingIntent(R.id.answer_btn, answerPendingIntent)
//        customView.setOnClickPendingIntent(R.id.decline_btn, endPendingIntent)

        val builder = NotificationCompat.Builder(applicationContext, "channelId").apply {
             setContentTitle("Bandyer Call") // or Bandyer Video Call
            .setContentText(name)
            .setSmallIcon(R.drawable.bandyer_z_audio_only) // or video icon
            .setSubText(subText)
            .setLargeIcon(avatar)
            .setContentIntent(callPendingIntent)
        }

        builder.setCustomContentView(customView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("channelId", "Incoming call", IMPORTANCE_HIGH).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableLights(true)
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

//        builder.addAction(R.drawable.ic_call_end_white_24dp, endTitle, endPendingIntent)
//        builder.addAction(R.drawable.ic_call, answerTitle, answerPendingIntent)
//        builder.addPerson("tel:" + user.phone)

        builder.priority = Notification.PRIORITY_MAX
        builder.color = -0xff0033
        builder.setVibrate(LongArray(0))
        builder.setCategory(Notification.CATEGORY_CALL)
        builder.setFullScreenIntent(PendingIntent.getActivity(this, 0, ringingIntent,  0), true)

        NotificationManagerCompat.from(applicationContext).notify(888, builder.build())
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
