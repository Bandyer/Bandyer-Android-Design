package com.bandyer.video_android_glass_ui

/**
 * Listener used to dispatch touch events
 */
interface TouchEventListener {
    /**
     * Called when a touch event has occurred
     *
     * @param event TouchEvent
     * @return Boolean
     */
    fun onTouch(event: TouchEvent): Boolean
}