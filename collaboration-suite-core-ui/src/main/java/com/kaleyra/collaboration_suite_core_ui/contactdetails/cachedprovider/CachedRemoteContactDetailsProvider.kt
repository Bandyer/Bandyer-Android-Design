package com.kaleyra.collaboration_suite_core_ui.contactdetails.cachedprovider

import com.kaleyra.collaboration_suite.Contacts
import com.kaleyra.collaboration_suite_core_ui.contactdetails.provider.RemoteContactDetailsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class CachedRemoteContactDetailsProvider(val contacts: Contacts, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    CachedContactDetailsProvider(RemoteContactDetailsProvider(contacts, ioDispatcher))