package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.new_smartglass.SmartGlassParticipantsFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.menu.MenuItem

class ParticipantsFragment : SmartGlassParticipantsFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        itemAdapter!!.add(MenuItem("Mario Rossi"))
        itemAdapter!!.add(MenuItem("Felice Trapasso"))
        itemAdapter!!.add(MenuItem("Francesco Sala"))

        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false
}