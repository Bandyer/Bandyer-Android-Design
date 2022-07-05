package com.kaleyra.collaboration_suite_core_ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.IBinder
import android.util.Log
import com.kaleyra.collaboration_suite_core_ui.common.BoundService
import com.kaleyra.collaboration_suite_core_ui.common.BoundServiceBinder
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.gotToLaunchingActivity
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Collaboration service
 */
abstract class CollaborationService : BoundService() {

    /**
     * On request new collaboration set up
     */
    abstract suspend fun onRequestNewCollaborationSetUp()
}

@SuppressLint("QueryPermissionsNeeded")
private fun getService(context: Context): ResolveInfo? {
    val serviceIntent = Intent().setAction("kaleyra_collaboration_configure").setPackage(context.packageName)
    val resolveInfo = context.packageManager.queryIntentServices(serviceIntent, PackageManager.GET_RESOLVED_FILTER)
    if (resolveInfo.size < 1) return null
    return resolveInfo[0]
}

private suspend fun getCollaborationService(): CollaborationService? = with(ContextRetainer.context) {
    val name = getService(this)?.serviceInfo?.name ?: return null
    val intent = Intent(this, Class.forName(name))
    startService(intent)
    return withTimeoutOrNull(1000L) {
        suspendCancellableCoroutine<CollaborationService> { continuation ->
            bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val collaborationService = (service as BoundServiceBinder).getService<CollaborationService>()
                    continuation.resumeWith(Result.success(collaborationService))
                }

                override fun onServiceDisconnected(name: ComponentName?) = Unit
            }, 0)
            continuation.invokeOnCancellation {
                Log.e("Collaboration", "Collaboration was not set up, you had 1s to do it. And you didn't do it. reason = $it")
            }
        }
    }
}

suspend fun whenCollaborationConfigured(block: (Boolean) -> Unit) {
    if (!CollaborationUI.isConfigured) getCollaborationService()?.onRequestNewCollaborationSetUp() ?: run {
        block(false)
        return ContextRetainer.context.gotToLaunchingActivity()
    }
    block(true)
}