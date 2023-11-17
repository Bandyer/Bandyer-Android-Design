/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.demo_video_sdk.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kaleyra.app_utilities.storage.LoginManager.getLoggedUser
import com.kaleyra.demo_video_sdk.DemoAppKaleyraVideoService
import com.kaleyra.demo_video_sdk.MainActivity
import com.kaleyra.demo_video_sdk.R.drawable
import com.kaleyra.demo_video_sdk.R.string
import com.kaleyra.demo_video_sdk.ui.utils.UserDetailsUtils.getUserImageBitmap
import com.kaleyra.video_common_ui.model.UserDetails
import com.kaleyra.video_utils.ContextRetainer.Companion.context
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject

class MissedNotificationPayloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        try {
            val loggedUser = getLoggedUser(context).takeIf { it.isNotBlank() } ?: return Result.failure()
            val payload = inputData.getString("payload")?.takeIf { it.isNotBlank() } ?: return Result.failure()
            Log.d(TAG, "Received payload\n$payload\nready to be processed.")
            val missedCall = JSONObject(payload)
            val userDetailsRequest = ArrayList<String>()
            val callData = missedCall.getJSONObject("data")
            val caller = callData.getString("caller_id")
            val calledUsers = callData.getJSONArray("called_users")
            var isValidNotification = false
            for (i in 0 until calledUsers.length()) {
                val calledUser = calledUsers.getJSONObject(i)
                if (calledUser.getString("user_id") == loggedUser) {
                    isValidNotification = calledUser.getString("status") == "not_answered"
                    break
                }
            }
            if (!isValidNotification) return Result.failure()
            userDetailsRequest.add(caller)
            val callbackUsers = callbackUsers(loggedUser, missedCall)
            runBlocking {
                val customUserDetailsProvider = DemoAppKaleyraVideoService.customUserDetailsProvider(context)
                customUserDetailsProvider?.invoke(userDetailsRequest)?.getOrNull()?.forEach {
                    showMissedCallNotification(it, payload.hashCode(), callbackUsers)
                } ?: userDetailsRequest.map { UserDetails(userId = it, name = it, image = Uri.EMPTY) }.forEach { showMissedCallNotification(it, payload.hashCode(), callbackUsers) }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            return Result.failure()
        }
        return Result.success()
    }

    private suspend fun showMissedCallNotification(userDetails: UserDetails, notificationId: Int, callbackUsers: ArrayList<String>) {
        val builder = Builder(applicationContext, notificationChannel)
            .setSmallIcon(drawable.ic_missed_call)
            .setContentTitle(userDetails.name)
            .setContentText(applicationContext.getString(string.missed_call))
            .setAutoCancel(true)
            .setLargeIcon(getUserImageBitmap(userDetails))
            .setContentIntent(openMainActivity())
            .addAction(drawable.ic_kaleyra_audio_call, applicationContext.getString(string.callback), callBack(callbackUsers, notificationId))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        if (callbackUsers.size == 1) builder.addAction(drawable.ic_kaleyra_chat, applicationContext.getString(string.chatback), chatBack(callbackUsers[0], notificationId))
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(notificationId, builder.build())
    }

    @Throws(JSONException::class)
    private fun callbackUsers(loggedUser: String, missedCall: JSONObject): ArrayList<String> {
        val caller = missedCall.getJSONObject("data").getString("caller_id")
        val callees = missedCall.getJSONObject("data").getJSONArray("called_users")
        val callbackUsers = ArrayList<String>()
        callbackUsers.add(caller)
        for (i in 0 until callees.length()) {
            val userId = callees.getJSONObject(i).getString("user_id")
            if (loggedUser != userId) callbackUsers.add(userId)
        }
        return callbackUsers
    }

    private fun chatBack(user: String, notification: Int): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(startChat, user)
        intent.putExtra(notificationId, notification)
        var flags = PendingIntent.FLAG_ONE_SHOT
        if (VERSION.SDK_INT >= VERSION_CODES.M) flags = flags or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(applicationContext, 1, intent, flags)
    }

    private fun callBack(callees: ArrayList<String>, notification: Int): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(startCall, callees)
        intent.putExtra(notificationId, notification)
        var flags = PendingIntent.FLAG_ONE_SHOT
        if (VERSION.SDK_INT >= VERSION_CODES.M) flags = flags or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(applicationContext, 2, intent, flags)
    }

    private fun openMainActivity(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java)
        var flags = PendingIntent.FLAG_ONE_SHOT
        if (VERSION.SDK_INT >= VERSION_CODES.M) flags = flags or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(applicationContext, 3, intent, flags)
    }

    private val notificationChannel: String
        private get() {
            if (VERSION.SDK_INT < VERSION_CODES.O) return event
            val channel = NotificationChannel(event, event, NotificationManager.IMPORTANCE_HIGH)
            channel.description = event
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            return event
        }

    companion object {
        private const val event = "on_missed_call"
        const val startCall = "startCall"
        const val startChat = "startChat"
        const val notificationId = "notificationId"

        @Throws(JSONException::class)
        fun isMissingCallMessage(payload: String?): Boolean {
            val webhookPayload = JSONObject(payload)
            return webhookPayload.getString("event") == event
        }

        fun cancelNotification(context: Context?, notificationId: Int) {
            val notificationManager = NotificationManagerCompat.from(context!!)
            notificationManager.cancel(notificationId)
        }

        private val TAG = MissedNotificationPayloadWorker::class.java.simpleName
    }
}