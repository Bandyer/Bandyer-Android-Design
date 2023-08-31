package com.kaleyra.collaboration_suite_core_ui.model

/**
 * User details provider
 */
typealias UserDetailsProvider = suspend (userIds: List<String>) -> Result<List<UserDetails>>
