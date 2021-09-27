package com.bandyer.app_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.app_design.R
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.menu.MenuFragment
import com.bandyer.video_android_glass_ui.menu.BandyerMenuItem
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setAlphaWithAnimation

class MenuFragment : MenuFragment(), TiltController.TiltListener,
                     ChatNotificationManager.NotificationListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

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
        activity.addNotificationListener(this)

        val view = super.onCreateView(inflater, container, savedInstanceState)

        itemAdapter!!.add(BandyerMenuItem("Attiva microfono", "Muta microfono"))
        itemAdapter!!.add(BandyerMenuItem("Attiva camera", "Muta camera"))
        itemAdapter!!.add(BandyerMenuItem("Volume"))
        itemAdapter!!.add(BandyerMenuItem("Zoom"))
        itemAdapter!!.add(BandyerMenuItem("Utenti"))
        itemAdapter!!.add(BandyerMenuItem("Chat"))

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

        bottomNavigation!!.setTapOnClickListener {
            tapBehaviour(currentMenuItemIndex)
        }

        bottomNavigation!!.setSwipeOnClickListener {
            rvMenu!!.horizontalSmoothScrollToNext(currentMenuItemIndex)
        }

        bottomNavigation!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity.removeNotificationListener(this)
    }

    override fun onTouch(event: TouchEvent): Boolean =
        when (event.type) {
            TouchEvent.Type.SWIPE_FORWARD  -> {
                if (event.source == TouchEvent.Source.KEY) {
                    rvMenu!!.horizontalSmoothScrollToNext(currentMenuItemIndex)
                    true
                } else false
            }
            TouchEvent.Type.SWIPE_BACKWARD -> {
                if (event.source == TouchEvent.Source.KEY) {
                    rvMenu!!.horizontalSmoothScrollToPrevious(currentMenuItemIndex)
                    true
                } else false
            }
            TouchEvent.Type.TAP            -> {
                tapBehaviour(currentMenuItemIndex)
            }
            TouchEvent.Type.SWIPE_DOWN     -> {
                findNavController().popBackStack()
                true
            }
            else                           -> super.onTouch(event)
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
            findNavController().navigate(R.id.action_menuFragment_to_smartglass_nav_graph_chat)
            true
        }
        else -> false
    }

    override fun onTilt(x: Float, y: Float, z: Float) = rvMenu!!.scrollBy((x * resources.displayMetrics.densityDpi / 5).toInt(), 0)

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

    override fun onShow() {
        root!!.setAlphaWithAnimation(0f, 100L)
    }

    override fun onExpanded() = Unit

    override fun onDismiss() {
        root!!.setAlphaWithAnimation(1f, 100L)
    }
}