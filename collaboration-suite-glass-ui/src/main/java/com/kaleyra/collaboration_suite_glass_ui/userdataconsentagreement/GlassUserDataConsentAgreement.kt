package com.kaleyra.collaboration_suite_glass_ui.userdataconsentagreement

import com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement.UserDataConsentAgreement

class GlassUserDataConsentAgreement(
    notificationInfo: NotificationInfo,
    activityInfo: ActivityInfo
) : UserDataConsentAgreement(
    GlassUserDataConsentAgreementActivity::class.java,
    notificationInfo,
    activityInfo
)