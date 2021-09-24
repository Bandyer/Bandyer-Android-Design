package com.bandyer.video_android_glass_ui

/**
 * Listener used to dispatch touch events
 */
interface BandyerGlassTouchEventListener {
    /**
     * Called when a touch event has occurred
     *
     * @param event BandyerGlassTouchEvent
     * @return Boolean
     */
    fun onSmartGlassTouchEvent(event: BandyerGlassTouchEvent): Boolean
}