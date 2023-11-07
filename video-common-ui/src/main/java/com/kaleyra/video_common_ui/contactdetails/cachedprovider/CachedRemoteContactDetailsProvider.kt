package com.kaleyra.video_common_ui.contactdetails.cachedprovider

import com.kaleyra.video.Contacts
import com.kaleyra.video_common_ui.contactdetails.provider.RemoteContactDetailsProvider

internal class CachedRemoteContactDetailsProvider(val contacts: Contacts) :
    CachedContactDetailsProvider(RemoteContactDetailsProvider(contacts))