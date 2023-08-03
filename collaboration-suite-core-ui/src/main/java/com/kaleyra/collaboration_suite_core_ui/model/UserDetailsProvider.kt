package com.kaleyra.collaboration_suite_core_ui.model

/**
 * User details provider
 */
interface UserDetailsProvider {

    /**
     *
     * @param userIds List<String>
     * @return Result<List<UserDetails>>
     */
    suspend fun userDetailsRequested(userIds: List<String>): Result<List<UserDetails>>
}