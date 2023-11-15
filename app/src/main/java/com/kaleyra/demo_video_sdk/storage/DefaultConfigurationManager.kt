package com.kaleyra.demo_video_sdk.storage

import android.annotation.SuppressLint
import android.content.Context
import com.kaleyra.demo_video_sdk.ui.custom_views.CallConfiguration
import com.kaleyra.demo_video_sdk.ui.custom_views.ChatConfiguration
import com.kaleyra.video_utils.ContextRetainer.Companion.context


object DefaultConfigurationManager {

    private const val preferenceKey = "DefaultConfigurationPrefs"
    private const val callConfigurationKey = "CALL_CONFIGURATION"
    private const val chatConfigurationKey = "CHAT_CONFIGURATION"

    private val prefs by lazy { context.getSharedPreferences(preferenceKey, Context.MODE_PRIVATE) }

    fun getDefaultCallConfiguration(): CallConfiguration = prefs.getString(callConfigurationKey, null)?.let { CallConfiguration.decode(it) } ?: CallConfiguration()
    fun getDefaultChatConfiguration(): ChatConfiguration = prefs.getString(chatConfigurationKey, null)?.let { ChatConfiguration.decode(it) } ?: ChatConfiguration()

    @SuppressLint("ApplySharedPref")
    fun saveDefaultCallConfiguration(callConfiguration: CallConfiguration) = prefs.edit().apply {
        putString(callConfigurationKey, callConfiguration.encode())
        commit()
    }

    @SuppressLint("ApplySharedPref")
    fun saveDefaultChatConfiguration(chatConfiguration: ChatConfiguration) = prefs.edit().apply {
        putString(chatConfigurationKey, chatConfiguration.encode())
        commit()
    }

    @SuppressLint("ApplySharedPref")
    fun clearAll() = prefs.edit().apply {
        clear()
        commit()
    }
}