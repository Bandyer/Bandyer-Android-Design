package com.kaleyra.collaboration_suite_glass_ui.common

/**
 * OnDestinationChangedListener. It has been created, even if [androidx.navigation.NavController.OnDestinationChangedListener] already exists,
 * because it is needed that onDestinationChanged is called in the fragments' onViewCreated method
 */
interface OnDestinationChangedListener {
    /**
     * Called when the navigation destination change
     *
     * @param destinationId Int
     */
    fun onDestinationChanged(destinationId: Int)
}