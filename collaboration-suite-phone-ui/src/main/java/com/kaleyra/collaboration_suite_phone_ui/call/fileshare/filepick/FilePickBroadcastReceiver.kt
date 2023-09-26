package com.kaleyra.collaboration_suite_phone_ui.call.fileshare.filepick

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal interface FilePickProvider {
    val fileUri: Flow<Uri>
}

internal class FilePickBroadcastReceiver : BroadcastReceiver() {

    companion object : FilePickProvider {
        const val ACTION_FILE_PICK_EVENT = "com.kaleyra.collaboration_suite_phone_ui.FILE_PICK_EVENT_ACTION"

        private val mUri: MutableSharedFlow<Uri> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
        override val fileUri: Flow<Uri> = mUri
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FILE_PICK_EVENT) return
        val uri = intent.getParcelableExtra<Uri>("uri") ?: return
        mUri.tryEmit(uri)
    }
}