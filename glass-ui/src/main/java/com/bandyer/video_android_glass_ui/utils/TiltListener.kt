package com.bandyer.video_android_glass_ui.utils

/**
 * A listener for the tilt events
 */
internal interface TiltListener {
    /**
     * Return the euler angles' degrees changes (azimuth/pitch/roll) around every [SensorManager.SENSOR_DELAY_GAME] Us
     *
     * @param deltaAzimuth The azimuth value
     * @param deltaPitch The pitch value
     * @param deltaRoll The roll value
     */
    fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float)
}