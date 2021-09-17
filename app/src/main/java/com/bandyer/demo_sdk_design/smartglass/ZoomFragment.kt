package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.BandyerGlassTouchEvent
import com.bandyer.video_android_glass_ui.settings.zoom.SmartGlassZoomFragment

class ZoomFragment : SmartGlassZoomFragment(), TiltController.TiltListener {

    private var tiltController: TiltController? = null

    private var deltaX = 0f

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

        bottomActionBar!!.setTapOnClickListener {
            findNavController().popBackStack()
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onSmartGlassTouchEvent(event: BandyerGlassTouchEvent): Boolean = when (event.type) {
        BandyerGlassTouchEvent.Type.SWIPE_FORWARD                                                                       -> {
            slider!!.increaseProgress(0.1f)
            true
        }
        BandyerGlassTouchEvent.Type.SWIPE_BACKWARD                                                                      -> {
            slider!!.decreaseProgress(0.1f)
            true
        }
        BandyerGlassTouchEvent.Type.TAP, BandyerGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else                                                                                                                                                    -> super.onSmartGlassTouchEvent(event)
    }

    override fun onTilt(x: Float, y: Float) {
        deltaX += x
        if (deltaX >= 2) {
            slider!!.increaseProgress(0.1f)
            deltaX = 0f
        }
        if (deltaX <= -2) {
            slider!!.decreaseProgress(0.1f)
            deltaX = 0f
        }
    }

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