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

package com.kaleyra.collaboration_suite_glass_ui.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils.isGoogleGlass
import java.util.*
import kotlin.math.abs

/**
 * TiltController
 *
 * @param ctx The context
 * @param listener The listener for tilt events.
 */
internal class TiltController constructor(
    ctx: Context,
    private val listener: TiltListener
) : SensorEventListener {

    private val windowManager = ContextCompat.getSystemService(ctx, WindowManager::class.java)!!
    private val sensorManager = ContextCompat.getSystemService(ctx, SensorManager::class.java)!!
    private val display =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ctx.display
        else windowManager.defaultDisplay

    private val rotationMatrix = FloatArray(16)
    private val remappedMatrix = FloatArray(16)
    private val orientation = FloatArray(9)

    private var accuracy = 0
    private var initialized = false

    // Buffers used to compute the moving average
    // These are used to smooth the azimuth/pitch/roll values and remove some noise on low frequencies
    private val bufferAzimuth: Queue<Float> = LinkedList()
    private val bufferPitch: Queue<Float> = LinkedList()
    private val bufferRoll: Queue<Float> = LinkedList()

    // The previous values of azimuth/pitch/roll are used compute the delta
    private var oldAzimuth = 0f
    private var oldPitch = 0f
    private var oldRoll = 0f

    // The delta difference between the new azimuth/pitch/roll value and the old one
    private var deltaAzimuth = 0f
    private var deltaPitch = 0f
    private var deltaRoll = 0f

    init {
        requestAllSensors()
    }

    /**
     * @suppress
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_LOW)
            return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR)
            updateRotation(event.values.clone())
    }

    /**
     * @suppress
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (this.accuracy != accuracy)
            this.accuracy = accuracy
    }

    /**
     * Update the rotation based on changes to the device's sensors.
     *
     * @param rotationVector The new rotation vectors.
     */
    private fun updateRotation(rotationVector: FloatArray) {
        // Get rotation's based on vector locations
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        // Remap the device's coordinate system
        remapMatrix(display!!.rotation, rotationMatrix, remappedMatrix)

        // Transform rotation matrix into azimuth/pitch/roll
        SensorManager.getOrientation(remappedMatrix, orientation)

        // Convert radians to degrees and flat
        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

        // Smooth the values and map them on 360 degrees
        val newAzimuth = mod(smoothValue(azimuth, bufferAzimuth), 360f)
        val newPitch = mod(smoothValue(pitch, bufferPitch), 360f)
        val newRoll = mod(smoothValue(roll, bufferRoll), 360f)

        // How many degrees has the users head rotated since last time
        val newDeltaAzimuth = applyThreshold(newAzimuth - oldAzimuth)
        val newDeltaPitch = applyThreshold(newPitch - oldPitch)
        val newDeltaRoll = applyThreshold(newRoll - oldRoll)

        // Update the delta values ignoring an eventual outlier
        if (abs(newDeltaAzimuth) < DELTA_PEAK)
            deltaAzimuth = newDeltaAzimuth
        if (abs(newDeltaPitch) < DELTA_PEAK)
            deltaPitch = newDeltaPitch
        if (abs(newDeltaRoll) < DELTA_PEAK)
            deltaRoll = newDeltaRoll

        // Ignore first head position in order to find base line
        if (!initialized) {
            initialized = true
            deltaAzimuth = 0f
            deltaPitch = 0f
            deltaRoll = 0f
        }

        oldAzimuth = newAzimuth
        oldPitch = newPitch
        oldRoll = newRoll

        listener.onTilt(deltaAzimuth, deltaPitch, deltaRoll)
    }

    /**
     * Smooth a new received value to attenuate the low frequency noise
     *
     * @param newValue A new value
     * @param buffer A buffer of previous stored values
     * @return The smoothed value
     */
    private fun smoothValue(newValue: Float, buffer: Queue<Float>): Float {
        buffer.add(newValue)
        if (buffer.size < WINDOW_SIZE) return newValue
        buffer.poll()
        val average = buffer.average().toFloat()
        return ATTENUATION_FACTOR * newValue + (1f - ATTENUATION_FACTOR) * average
    }

    /**
     * Remap the device's coordinate system of [matrix] and store the result in [newMatrix]
     *
     * @param displayRotation Int?
     * @param matrix FloatArray
     * @param newMatrix FloatArray
     */
    private fun remapMatrix(displayRotation: Int?, matrix: FloatArray, newMatrix: FloatArray) {
        val worldAxes = computeWorldAxisForDeviceAxes(displayRotation)

        SensorManager.remapCoordinateSystem(
            matrix,
            worldAxes.first,
            worldAxes.second,
            newMatrix
        )
    }

    /**
     * Compute a pair of values. The first value is the world axis for the device x axis and the second one is the world axis for the device y axis
     * The device rotation is used to compute the updated axes
     *
     * @param displayRotation Int?
     * @return Pair<Int, Int>
     */
    private fun computeWorldAxisForDeviceAxes(displayRotation: Int?): Pair<Int, Int> =
        when {
            isGoogleGlass -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y)
            displayRotation == Surface.ROTATION_90 -> Pair(
                SensorManager.AXIS_Z,
                SensorManager.AXIS_MINUS_X
            )
            displayRotation == Surface.ROTATION_180 -> Pair(
                SensorManager.AXIS_MINUS_X,
                SensorManager.AXIS_MINUS_Z
            )
            displayRotation == Surface.ROTATION_270 -> Pair(
                SensorManager.AXIS_MINUS_Z,
                SensorManager.AXIS_X
            )
            else -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Z)
        }

    /**
     * Apply a minimum value to the input
     * If the value is below the threshold, return ignore small head movements
     *
     * @param value The value to inspect
     * @return The value of input if within the threshold, or 0 if it is outside
     */
    private fun applyThreshold(value: Float): Float =
        if (abs(value) > THRESHOLD_MOTION) value else 0f

    /**
     * Calculates {a mod b} in a way that respects negative values
     * (for instance, mod(-1, 5) == 4, rather than -1)
     *
     * @param a the dividend
     * @param b the divisor
     * @return a mod b
     */
    private fun mod(a: Float, b: Float) = (a % b + b) % b

    /**
     * Request access to sensors
     */
    fun requestAllSensors() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_GAME
        )
        // The rotation vector sensor doesn't give us accuracy updates, so we observe the
        // magnetic field sensor solely for those.
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    /**
     * Release the sensors when they are no longer used
     */
    fun releaseAllSensors() {
        sensorManager.unregisterListener(this)
    }

    private companion object {
        const val THRESHOLD_MOTION = 0.1f

        // How much values to store for the moving average calculation
        const val WINDOW_SIZE = 10

        // The weight of the new value receive
        const val ATTENUATION_FACTOR = 0.2f

        // The delta peak value
        const val DELTA_PEAK = 20
    }
}