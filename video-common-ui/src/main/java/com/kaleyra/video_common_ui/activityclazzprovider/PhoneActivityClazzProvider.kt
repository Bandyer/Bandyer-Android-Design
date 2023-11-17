package com.kaleyra.video_common_ui.activityclazzprovider

internal object PhoneActivityClazzProvider: ActivityClazzProvider(
    callActivityClassName = "com.kaleyra.video_sdk.call.PhoneCallActivity",
    chatActivityClassName = "com.kaleyra.video_sdk.chat.PhoneChatActivity",
    termsAndConditionsActivityClassName = "com.kaleyra.video_sdk.termsandconditions.PhoneTermsAndConditionsActivity"
)