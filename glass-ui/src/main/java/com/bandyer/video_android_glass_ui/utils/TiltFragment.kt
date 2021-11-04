package com.bandyer.video_android_glass_ui.utils

import androidx.fragment.app.Fragment

/**
 * A fragment which observes the tilt events. Set the [tiltListener] to start listening to those events.
 */
internal abstract class TiltFragment : Fragment() {

    /**
     * The tilt controller
     */
    private var tiltController: TiltController? = null

    /**
     * The tilt listener
     */
    var tiltListener: TiltListener? = null
        set(value) {
            tiltController = if(value == null) tiltController!!.releaseAllSensors().let { null } else TiltController(requireContext(), value)
            field = value
        }

    /**
     * @suppress
     */
    override fun onResume() {
        super.onResume()
        tiltController?.requestAllSensors()
    }

    /**
     * @suppress
     */
    override fun onPause() {
        super.onPause()
        tiltController?.releaseAllSensors()
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        tiltController = null
    }
}