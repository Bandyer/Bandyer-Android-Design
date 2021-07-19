package com.bandyer.sdk_design.new_smartglass

interface SmartGlassTouchEventListener {
    fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}

interface BottomBarHolder {
    fun showBottomBar()
    fun hideBottomBar()
}