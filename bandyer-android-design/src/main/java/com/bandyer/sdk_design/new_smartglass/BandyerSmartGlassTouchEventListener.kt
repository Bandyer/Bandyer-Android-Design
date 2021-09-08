package com.bandyer.sdk_design.new_smartglass

/**
 * SmartGlassTouchEventListener
 */
interface BandyerSmartGlassTouchEventListener {
    fun onSmartGlassTouchEvent(event: BandyerSmartGlassTouchEvent): Boolean
}