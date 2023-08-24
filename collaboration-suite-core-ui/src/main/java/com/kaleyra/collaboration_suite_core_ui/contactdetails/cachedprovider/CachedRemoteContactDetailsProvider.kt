package com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider

import com.kaleyra.collaboration_suite.Contacts
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.RemoteContactDetailsProvider

internal class CachedRemoteContactDetailsProvider(val contacts: Contacts) :
    CachedContactDetailsProvider(RemoteContactDetailsProvider(contacts))