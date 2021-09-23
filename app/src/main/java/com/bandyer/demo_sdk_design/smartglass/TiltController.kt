package com.bandyer.demo_sdk_design.smartglass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.bandyer.demo_sdk_design.R
import java.util.*
import kotlin.math.abs

/**
 * TiltController
 *
 * @param ctx The context
 * @param listener The listener for tilt events.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class TiltController constructor(
    ctx: Context,
    private val listener: TiltListener
) : SensorEventListener {

    interface TiltListener {
        /**
         * Return the euler angles (azimuth/pitch/roll)
         *
         * @param azimuth The azimuth value
         * @param pitch The pitch value
         * @param roll The roll value
         */
        fun onTilt(azimuth: Float, pitch: Float, roll: Float)
    }

    private val isGoogleGlasses =
        Build.MODEL == ctx.resources.getString(R.string.bandyer_glass_google_model)

    private val windowManager = ctx.getSystemService(WindowManager::class.java)
    private val sensorManager = ctx.getSystemService(SensorManager::class.java)
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

    // The delta for the euler angles
    private var deltaAzimuth = 0f
    private var deltaPitch = 0f
    private var deltaRoll = 0f

    init {
        requestAllSensors()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_LOW)
            return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR)
            updateRotation(event.values.clone())
    }

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

        remapMatrix()

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
        if (buffer.size < WINDOW_SIZE) return 0f
        buffer.poll()
        val average = buffer.average().toFloat()
        return ATTENUATION_FACTOR * newValue + (1f - ATTENUATION_FACTOR) * average
    }

    /**
     * Remap the device's coordinate system
     */
    private fun remapMatrix() {
        val worldAxes = computeWorldAxisForDeviceAxes()

        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            worldAxes.first,
            worldAxes.second,
            remappedMatrix
        )
    }

    /**
     * Compute a pair of values. The first value is the world axis for the device x axis and the second one is the world axis for the device y axis
     *
     * @return Pair<Int, Int>
     */
    private fun computeWorldAxisForDeviceAxes(): Pair<Int, Int> =
        when {
            isGoogleGlasses -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y)
            display?.rotation == Surface.ROTATION_90 -> Pair(
                SensorManager.AXIS_Z,
                SensorManager.AXIS_MINUS_X
            )
            display?.rotation == Surface.ROTATION_180 -> Pair(
                SensorManager.AXIS_MINUS_X,
                SensorManager.AXIS_MINUS_Z
            )
            display?.rotation == Surface.ROTATION_270 -> Pair(
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