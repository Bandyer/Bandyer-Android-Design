package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.menu.MenuItem
import com.bandyer.sdk_design.new_smartglass.menu.SmartGlassMenuFragment

class MenuFragment : SmartGlassMenuFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as SmartGlassActivity).showStatusBar()
        val view = super.onCreateView(inflater, container, savedInstanceState)

        fastAdapter.onClickListener = { _, _, item, _ ->
            item.isActive = !item.isActive
            false
        }

        itemAdapter.add(MenuItem("Attiva microfono", "Muta microfono"))
        itemAdapter.add(MenuItem("Attiva camera", "Muta camera"))
        itemAdapter.add(MenuItem("Volume"))
        itemAdapter.add(MenuItem("Zoom"))
        itemAdapter.add(MenuItem("Chat"))

        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false
}