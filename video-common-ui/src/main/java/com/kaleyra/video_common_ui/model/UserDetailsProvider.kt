package com.kaleyra.video_common_ui.model

/**
 * User details provider
 */
typealias UserDetailsProvider = suspend (userIds: List<String>) -> Result<List<UserDetails>>
