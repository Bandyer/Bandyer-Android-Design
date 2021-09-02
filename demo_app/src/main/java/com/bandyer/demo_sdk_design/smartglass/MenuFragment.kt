package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.menu.MenuItem
import com.bandyer.sdk_design.new_smartglass.menu.SmartGlassMenuFragment

class MenuFragment : SmartGlassMenuFragment(), TiltController.TiltListener {

    private var tiltController: TiltController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tiltController =
                TiltController(
                    requireContext(),
                    this
                )
        }
    }

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

        fastAdapter!!.onClickListener = { _, _, _, position ->
            tapBehaviour(position)
        }

        bottomActionBar!!.setTapOnClickListener {
            tapBehaviour(currentMenuItemIndex)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.TAP -> {
            tapBehaviour(currentMenuItemIndex)
        }
        SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }

    private fun tapBehaviour(itemIndex: Int) = when (itemIndex) {
        0, 1 -> {
            val isActivated = itemAdapter!!.getAdapterItem(currentMenuItemIndex).isActivated
            itemAdapter!!.getAdapterItem(currentMenuItemIndex).isActivated = !isActivated
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

    override fun onTilt(x: Float, y: Float) = rvMenu!!.scrollBy((x * 40).toInt(), 0)

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.requestAllSensors()
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.releaseAllSensors()
    }
}