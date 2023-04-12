package com.kaleyra.collaboration_suite_core_ui.notification.fileshare

import android.app.Notification
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.sharedfolder.SharedFile
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

interface FileShareNotificationDelegate {

    companion object {
        const val EXTRA_DOWNLOAD_ID = "com.kaleyra.collaboration_suite_core_ui.EXTRA_DOWNLOAD_ID"
    }

    fun syncFileShareNotification(
        call: Call,
        activityClazz: Class<*>,
        scope: CoroutineScope
    ) {
        var lastNotifiedDownload: SharedFile? = null
        val me = call.participants.value.me
        call.sharedFolder.files
            .transform { files ->
                val lastDownload = files.lastOrNull { file -> file.sender.userId != me.userId } ?: return@transform
                if (lastNotifiedDownload != lastDownload) emit(lastDownload)
                lastNotifiedDownload = lastDownload
            }
            .onEach {
                val notification = buildNotification(call, it, activityClazz)
                NotificationManager.notify(it.id.hashCode(), notification)
            }
            .onCompletion {
                call.sharedFolder.files.value.forEach {
                    NotificationManager.cancel(it.id.hashCode())
                }
            }
            .launchIn(scope)
    }

    private suspend fun buildNotification(call: Call, sharedFile: SharedFile, activityClazz: Class<*>): Notification {
        val participants = call.participants.first()
        val participant = participants.others.firstOrNull { it.userId == sharedFile.sender.userId }
        val username = participant?.displayName?.first() ?: ""
        return NotificationManager.buildIncomingFileNotification(username, sharedFile.id, activityClazz)
    }
}