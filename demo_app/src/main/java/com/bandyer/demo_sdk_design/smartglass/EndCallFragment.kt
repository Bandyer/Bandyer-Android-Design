package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.new_smartglass.SmartGlassEndCallFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent

class EndCallFragment: SmartGlassEndCallFragment() {

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false
}