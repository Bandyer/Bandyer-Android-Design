package com.bandyer.video_android_glass_ui

import android.os.Bundle
import com.bandyer.video_android_glass_ui.utils.TiltController

/**
 * A base fragment with tilt functionality
 */
abstract class TiltFragment : BaseFragment(), TiltController.TiltListener {

    private var tiltController: TiltController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tiltController = TiltController(requireContext(), this)
    }

    override fun onResume() {
        super.onResume()
        tiltController!!.requestAllSensors()
    }

    override fun onPause() {
        super.onPause()
        tiltController!!.releaseAllSensors()
    }
}