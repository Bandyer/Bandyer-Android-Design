/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_glasses_sdk.utils

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