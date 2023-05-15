package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationActionReceiver
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow

class PhoneCallActivity : FragmentActivity() {

    private val shouldShowFileShare = MutableStateFlow(false)

    private val isInPipMode = MutableStateFlow(false)

    private var pipRect = Rect()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentAction(intent)
        updatePipParams()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                CallScreen(
                    shouldShowFileShareComponent = shouldShowFileShare.collectAsStateWithLifecycle().value,
                    isInPipMode = isInPipMode.collectAsStateWithLifecycle().value,
                    onFileShareDisplayed = { shouldShowFileShare.value = false },
                    onFirstStreamPositioned = { pipRect = it },
                    onBackPressed = this::finishAndRemoveTask
                )
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }

    private val isPipSupported by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        else false
    }

    private fun updatePipParams(): PictureInPictureParams? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder()
                .setAspectRatio(getAspectRatioRational(pipRect))
                .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) setAutoEnterEnabled(false) }
                .build()
        } else null
    }

    private fun getAspectRatioRational(rect: Rect): Rational {
        val width = rect.width()
        val height = rect.height()
        return if (width > height) Rational(16, 9)
        else Rational(9, 16)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isPipSupported) return
        updatePipParams()?.let { params ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(params)
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
