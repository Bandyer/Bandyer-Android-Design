package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.menu.MenuItem
import com.bandyer.sdk_design.new_smartglass.menu.SmartGlassMenuFragment

class MenuFragment : SmartGlassMenuFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        itemAdapter!!.add(MenuItem("Attiva microfono", "Muta microfono"))
        itemAdapter!!.add(MenuItem("Attiva camera", "Muta camera"))
        itemAdapter!!.add(MenuItem("Volume"))
        itemAdapter!!.add(MenuItem("Zoom"))
        itemAdapter!!.add(MenuItem("Utenti"))
        itemAdapter!!.add(MenuItem("Chat"))

        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = when (event) {
        SmartGlassTouchEvent.Event.TAP -> {
            when (currentMenuItemIndex) {
                0, 1 -> {
                    val isActive = itemAdapter!!.getAdapterItem(currentMenuItemIndex).isActive
                    itemAdapter!!.getAdapterItem(currentMenuItemIndex).isActive = !isActive
                    true
                }
                2 -> {
                    findNavController().navigate(R.id.action_menuFragment_to_volumeFragment)
                    true
                }
                3 -> {
                    findNavController().navigate(R.id.action_menuFragment_to_zoomFragment)
                    true
                }
                4 -> {
                    findNavController().navigate(R.id.action_menuFragment_to_participantsFragment)
                    true
                }
                5 -> {
                    findNavController().navigate(R.id.action_menuFragment_to_chatFragment)
                    true
                }
                else -> false
            }
        }
        SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}