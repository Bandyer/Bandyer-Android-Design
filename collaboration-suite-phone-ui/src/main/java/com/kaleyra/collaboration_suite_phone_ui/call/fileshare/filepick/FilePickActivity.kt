/*
 * Copyright (C) 2020 Bandyer S.r.l. All Rights Reserved.
 * See LICENSE.txt for licensing information
 */

package com.kaleyra.collaboration_suite_phone_ui.call.fileshare.filepick

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.filepick.FilePickBroadcastReceiver.Companion.ACTION_FILE_PICK_EVENT

internal class FilePickActivity : ComponentActivity() {

    companion object {

        private var instance: FilePickActivity? = null

        fun show(context: Context) {
            val applicationContext = context.applicationContext
            instance?.let {
                applicationContext.startActivity(it.intent)
                return
            }
            val intent = Intent(applicationContext, FilePickActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            applicationContext.startActivity(intent)
        }

        fun close() {
            instance?.cleanInstance()
        }
    }

    private var getContent: ActivityResultLauncher<Unit>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        getContent = registerForActivityResult(CustomOpenDocument()) { uri: Uri? ->
            uri?.let { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            val intent = Intent(ACTION_FILE_PICK_EVENT)
            intent.`package` = packageName
            intent.putExtra("uri", uri)
            sendOrderedBroadcast(intent, null)
            cleanInstance()
        }
        pickFile()
    }

    override fun onDestroy() {
        cleanInstance()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        pickFile()
    }

    private fun pickFile() {
        if (isFinishing) return
        getContent?.launch(Unit)
    }

    private fun cleanInstance() {
        getContent?.unregister()
        getContent = null
        instance = null
        finishAndRemoveTask()
    }

    class CustomOpenDocument : ActivityResultContract<Unit, Uri?>() {

        @CallSuper
        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*")
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        override fun getSynchronousResult(context: Context, input: Unit): SynchronousResult<Uri?>? {
            return null
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent?.takeIf { resultCode == RESULT_OK }?.data
        }
    }
}