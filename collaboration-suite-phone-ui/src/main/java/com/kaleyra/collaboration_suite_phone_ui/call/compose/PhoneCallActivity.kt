package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationActionReceiver
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow

class PhoneCallActivity : FragmentActivity() {

    private var shouldShowFileShare = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentAction(intent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                CallScreen(
                    shouldShowFileShareComponent = shouldShowFileShare.collectAsStateWithLifecycle().value,
                    onFileShareDisplayed = { shouldShowFileShare.value = false },
                    onBackPressed = this::finishAndRemoveTask
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentAction(intent)
    }

    private fun handleIntentAction(intent: Intent) {
        val action = intent.extras?.getString("action") ?: return
        when (action) {
            CallNotificationActionReceiver.ACTION_ANSWER, CallNotificationActionReceiver.ACTION_HANGUP -> {
                sendBroadcast(Intent(this, CallNotificationActionReceiver::class.java).apply {
                    this.action = action
                })
            }
            FileShareNotificationActionReceiver.ACTION_DOWNLOAD -> {
                sendBroadcast(Intent(this, FileShareNotificationActionReceiver::class.java).apply {
                    this.action = action
                    this.putExtras(intent)
                })
                shouldShowFileShare.value = true
            }
        }

    }
}
