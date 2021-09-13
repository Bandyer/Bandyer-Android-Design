package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.demo_sdk_design.R
import com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent
import com.bandyer.video_android_glass_ui.menu.BandyerMenuItem
import com.bandyer.video_android_glass_ui.menu.SmartGlassMenuFragment
import com.bandyer.sdk_design.new_smartglass.smoothScrollToNext
import com.bandyer.sdk_design.new_smartglass.smoothScrollToPrevious

class MenuFragment : com.bandyer.video_android_glass_ui.menu.SmartGlassMenuFragment(), TiltController.TiltListener {

    private var tiltController: TiltController? = null

    private var currentMenuItemIndex = 0

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

        itemAdapter!!.add(com.bandyer.video_android_glass_ui.menu.BandyerMenuItem("Attiva microfono", "Muta microfono"))
        itemAdapter!!.add(com.bandyer.video_android_glass_ui.menu.BandyerMenuItem("Attiva camera", "Muta camera"))
        itemAdapter!!.add(com.bandyer.video_android_glass_ui.menu.BandyerMenuItem("Volume"))
        itemAdapter!!.add(com.bandyer.video_android_glass_ui.menu.BandyerMenuItem("Zoom"))
        itemAdapter!!.add(com.bandyer.video_android_glass_ui.menu.BandyerMenuItem("Utenti"))
        itemAdapter!!.add(com.bandyer.video_android_glass_ui.menu.BandyerMenuItem("Chat"))

        // add scroll listener
        rvMenu!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager
                val foundView = snapHelper!!.findSnapView(layoutManager) ?: return
                currentMenuItemIndex = layoutManager!!.getPosition(foundView)
            }
        })

        fastAdapter!!.onClickListener = { _, _, _, position ->
            tapBehaviour(position)
        }

        bottomActionBar!!.setTapOnClickListener {
            tapBehaviour(currentMenuItemIndex)
        }

        bottomActionBar!!.setSwipeOnClickListener {
            rvMenu!!.smoothScrollToNext(currentMenuItemIndex)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onSmartGlassTouchEvent(event: com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent): Boolean = when (event.type) {
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.SWIPE_FORWARD  -> {
            if (event.source == com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Source.KEY) {
                rvMenu!!.smoothScrollToNext(currentMenuItemIndex)
                true
            } else false
        }
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            if (event.source == com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Source.KEY) {
                rvMenu!!.smoothScrollToPrevious(currentMenuItemIndex)
                true
            } else false
        }
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.TAP            -> {
            tapBehaviour(currentMenuItemIndex)
        }
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.SWIPE_DOWN     -> {
            findNavController().popBackStack()
            true
        }
        else                                                                               -> super.onSmartGlassTouchEvent(event)
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