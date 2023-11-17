package com.kaleyra.video_common_ui.activityclazzprovider

internal abstract class ActivityClazzProvider(
    private val callActivityClassName: String,
    private val chatActivityClassName: String,
    private val termsAndConditionsActivityClassName: String,
    private val customChatNotificationClassName: String? = null
) {

    fun getActivityClazzConfiguration(): ActivityClazzConfiguration? {
        val callActivityClazz = getClassForName(callActivityClassName)
        val chatActivityClazz = getClassForName(chatActivityClassName)
        val termsAndConditionsActivityClazz = getClassForName(termsAndConditionsActivityClassName)
        if (callActivityClazz == null || chatActivityClazz == null || termsAndConditionsActivityClazz == null) return null
        return customChatNotificationClassName?.let {
            val chatNotificationActivityClazz = getClassForName(customChatNotificationClassName) ?: return null
            ActivityClazzConfiguration(callClazz = callActivityClazz, chatClazz = chatActivityClazz, termsAndConditionsClazz = termsAndConditionsActivityClazz, customChatNotificationClazz = chatNotificationActivityClazz)
        } ?: ActivityClazzConfiguration(callClazz = callActivityClazz, chatClazz = chatActivityClazz, termsAndConditionsClazz = termsAndConditionsActivityClazz)
    }

    private fun getClassForName(className: String): Class<*>? = kotlin.runCatching { Class.forName(className) }.getOrNull()
}

