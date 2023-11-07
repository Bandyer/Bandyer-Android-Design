package com.kaleyra.video_common_ui.notification.fileshare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileShareVisibilityObserver internal constructor(): BroadcastReceiver() {
    /**
     * @suppress
     */
    companion object {
        const val ACTION_FILE_SHARE_DISPLAYED = "com.kaleyra.video_common_ui.FILE_SHARE_DISPLAYED"

        const val ACTION_FILE_SHARE_NOT_DISPLAYED = "com.kaleyra.video_common_ui.FILE_SHARE_NOT_DISPLAYED"

        private val _isDisplayed: MutableStateFlow<Boolean> = MutableStateFlow(false)
        internal val isDisplayed: StateFlow<Boolean> = _isDisplayed
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FILE_SHARE_DISPLAYED -> _isDisplayed.value = true
            ACTION_FILE_SHARE_NOT_DISPLAYED -> _isDisplayed.value = false
            else -> Unit
        }
    }
}