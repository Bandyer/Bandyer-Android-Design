package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.filepick

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class FilePickBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_FILE_PICK_EVENT = "com.kaleyra.collaboration_suite_phone_ui.FILE_PICK_EVENT_ACTION"

        private val mUri: MutableSharedFlow<Uri> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
        val uri: SharedFlow<Uri> = mUri.asSharedFlow()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FILE_PICK_EVENT) return
        val uri = intent.getParcelableExtra<Uri>("uri") ?: return
        mUri.tryEmit(uri)
    }

}