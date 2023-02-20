package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.filepick

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun FilePickBroadcastReceiver(
    filePickerAction: String,
    onFilePickEvent: (uri: Uri) -> Unit
) {
    val context = LocalContext.current
    val currentOnFilePickEvent by rememberUpdatedState(onFilePickEvent)

    DisposableEffect(context, filePickerAction) {
        val intentFilter = IntentFilter(filePickerAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != FilePickActivity.ACTION_FILE_PICK_EVENT) return

                val uri = intent.getParcelableExtra<Uri>("uri") ?: return
                currentOnFilePickEvent(uri)
            }
        }

        context.registerReceiver(broadcast, intentFilter)

        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}