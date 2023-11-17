package com.kaleyra.video_common_ui.activityclazzprovider

internal object GlassActivityClazzProvider: ActivityClazzProvider(
    callActivityClassName = "com.kaleyra.video_glasses_sdk.call.GlassCallActivity",
    chatActivityClassName = "com.kaleyra.video_glasses_sdk.chat.GlassChatActivity",
    termsAndConditionsActivityClassName = "com.kaleyra.video_glasses_sdk.termsandconditions.GlassTermsAndConditionsActivity",
    customChatNotificationClassName = "com.kaleyra.video_glasses_sdk.chat.notification.GlassChatNotificationActivity"
)