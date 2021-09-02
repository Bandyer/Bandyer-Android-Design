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
         * Return the degrees of the movement
         *
         * @param x The degrees on X axis
         * @param y The degrees Y axis
         */
        fun onTilt(x: Float, y: Float)
    }

    private val windowManager = ctx.getSystemService(WindowManager::class.java)
    private val sensorManager = ctx.getSystemService(SensorManager::class.java)
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val display =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ctx.display
        else windowManager.defaultDisplay


    private val rotationMatrix = FloatArray(9)
    private val adjustedRotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var initialized = false

    private var lastAccuracy = 0
    private var oldZ = 0f
    private var oldX = 0f

    init {
        sensorManager.registerListener(this, rotationSensor, SENSOR_DELAY_MICROS)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (lastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return

        if (event.sensor == rotationSensor)
            updateRotation(event.values.clone())
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (lastAccuracy != accuracy)
            lastAccuracy = accuracy
    }

    /**
     * Update the rotation based on changes to the device's sensors.
     *
     * @param rotationVector The new rotation vectors.
     */
    private fun updateRotation(rotationVector: FloatArray) {
        // Get rotation's based on vector locations
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        val worldAxisForDeviceAxisX: Int
        val worldAxisForDeviceAxisY: Int

        when (display?.rotation) {
            Surface.ROTATION_90 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z
            }
            Surface.ROTATION_270 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z
                worldAxisForDeviceAxisY = SensorManager.AXIS_X
            }
            else -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z
            }
        }

        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            worldAxisForDeviceAxisX,
            worldAxisForDeviceAxisY,
            adjustedRotationMatrix
        )

        // Transform rotation matrix into azimuth/pitch/roll
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)

        // Convert radians to degrees and flat
        val newX = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val newZ = Math.toDegrees(orientation[0].toDouble()).toFloat()

        // How many degrees has the users head rotated since last time.
        var deltaX = applyThreshold(newX - oldX)
        var deltaZ = applyThreshold(newZ - oldZ)

        // Ignore first head position in order to find base line
        if (!initialized) {
            initialized = true
            deltaX = 0f
            deltaZ = 0f
        }
        oldX = newX
        oldZ = newZ

        listener.onTilt(deltaZ, deltaX)
    }

    /**
     * Apply a minimum value to the input
     * If the value is below the threshold, return zero to remove noise
     *
     * @param value The value to inspect
     * @return The value of input if within the threshold, or 0 if it is outside
     */
    private fun applyThreshold(value: Float): Float =
        if (abs(value) > THRESHOLD_MOTION) value else 0f

    /**
     * Request access to sensors
     */
    fun requestAllSensors() {
        sensorManager.registerListener(this, rotationSensor, SENSOR_DELAY_MICROS)
    }

    /**
     * Release the sensors when they are no longer used
     */
    fun releaseAllSensors() {
        sensorManager.unregisterListener(this, rotationSensor)
    }

    private companion object {
        const val THRESHOLD_MOTION = 0.1f
        const val SENSOR_DELAY_MICROS = 32 * 1000 // 32ms
    }
}