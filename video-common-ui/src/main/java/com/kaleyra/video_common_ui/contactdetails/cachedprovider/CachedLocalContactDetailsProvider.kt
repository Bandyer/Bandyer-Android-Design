package com.kaleyra.video_common_ui.contactdetails.cachedprovider

import com.kaleyra.video_common_ui.contactdetails.provider.LocalContactDetailsProvider
import com.kaleyra.video_common_ui.model.UserDetailsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class CachedLocalContactDetailsProvider(val userDetailsProvider: UserDetailsProvider, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    CachedContactDetailsProvider(LocalContactDetailsProvider(userDetailsProvider, ioDispatcher))