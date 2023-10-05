package com.kaleyra.collaboration_suite_phone_ui.call

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_core_ui.notification.CallNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.proximity.ProximityCallActivity
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOff
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.turnScreenOn
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getScreenAspectRatio
import com.kaleyra.collaboration_suite_phone_ui.call.screen.CallScreen
import com.kaleyra.collaboration_suite_phone_ui.extensions.RationalExtensions.coerceRationalForPip
import kotlinx.coroutines.flow.MutableStateFlow

class PhoneCallActivity : FragmentActivity(), ProximityCallActivity {

    private companion object {
        var pictureInPictureAspectRatio: Rational = Rational(9, 16)

        val isInPipMode: MutableStateFlow<Boolean> = MutableStateFlow(false)

        val shouldShowFileShare: MutableStateFlow<Boolean> = MutableStateFlow(false)

        var isActivityFinishing: Boolean = false

        var isInForeground: Boolean = false

        var isFileShareDisplayed: Boolean = false

        var isWhiteboardDisplayed: Boolean = false
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = enterPipModeIfSupported()
    }

    override val disableProximity: Boolean
        get() = !isInForeground || isInPipMode.value || isWhiteboardDisplayed || isFileShareDisplayed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentAction(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updatePipParams()?.let { setPictureInPictureParams(it) }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContent {
            CallScreen(
                shouldShowFileShareComponent = shouldShowFileShare.collectAsStateWithLifecycle().value,
                isInPipMode = isInPipMode.collectAsStateWithLifecycle().value,
                onEnterPip = ::onUserLeaveHint,
                onFileShareVisibility = {
                    isFileShareDisplayed = it
                    if (it) shouldShowFileShare.value = false
                },
                onWhiteboardVisibility = { isWhiteboardDisplayed = it },
                onPipAspectRatio = { aspectRatio ->
                    pictureInPictureAspectRatio = if (aspectRatio == Rational.NaN) getScreenAspectRatio() else aspectRatio.coerceRationalForPip()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        updatePipParams()?.let { setPictureInPictureParams(it) }
                    }
                },
                onActivityFinishing = { isActivityFinishing = true },
            )
        }
        turnScreenOn()
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOff()
    }

    override fun disableWindowTouch() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun enableWindowTouch() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    @SuppressLint("MissingSuperCall")
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }

    private val isPipSupported by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
        else false
    }

    private fun enterPipModeIfSupported() {
        if (!isPipSupported) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updatePipParams()?.let { params ->
                enterPictureInPictureMode(params)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePipParams() =
        PictureInPictureParams.Builder()
            .setAspectRatio(pictureInPictureAspectRatio)
            .build()

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipModeIfSupported()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (handleIntentAction(intent)) return
        restartActivityIfCurrentCallIsEnded(intent)
    }

    private fun handleIntentAction(intent: Intent): Boolean {
        val action = intent.extras?.getString("action") ?: return false
        return when (action) {
            CallNotificationActionReceiver.ACTION_ANSWER, CallNotificationActionReceiver.ACTION_HANGUP -> {
                sendBroadcast(Intent(this, CallNotificationActionReceiver::class.java).apply {
                    this.action = action
                })
                true
            }

            FileShareNotificationActionReceiver.ACTION_DOWNLOAD -> {
                sendBroadcast(Intent(this, FileShareNotificationActionReceiver::class.java).apply {
                    this.action = action
                    this.putExtras(intent)
                })
                shouldShowFileShare.value = true
                true
            }

            else -> false
        }
    }

    private fun restartActivityIfCurrentCallIsEnded(intent: Intent) {
        if (isActivityFinishing && Intent.FLAG_ACTIVITY_NEW_TASK.let { intent.flags.and(it) == it }) {
            finishAndRemoveTask()
            startActivity(intent)
        }
    }

}
