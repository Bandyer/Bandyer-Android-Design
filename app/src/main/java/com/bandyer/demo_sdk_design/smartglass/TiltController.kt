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
    private val display =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ctx.display
        else windowManager.defaultDisplay

    private val rotationMatrix = FloatArray(16)
    private val orientation = FloatArray(9)

    private var accuracy = 0
    private var initialized = false

    private var oldZ = 0f
    private var oldX = 0f

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
        if(this.accuracy != accuracy)
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
            rotationMatrix
        )

        // Transform rotation matrix into azimuth/pitch/roll
        SensorManager.getOrientation(rotationMatrix, orientation)

        // Convert radians to degrees and flat
        val newX = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val newZ = Math.toDegrees(orientation[0].toDouble()).toFloat()

        // How many degrees has the users head rotated since last time.
        var deltaX = applyThreshold(angularRounding(newX - oldX))
        var deltaZ = applyThreshold(angularRounding(newZ - oldZ))

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
     * Adjust the angle of rotation to take into account on the device orientation.
     *
     * @param rotation The rotation.
     * @return The rotation taking into account the device orientation.
     */
    private fun angularRounding(rotation: Float) =
        when {
            rotation >= 180.0f -> rotation - 360.0f
            rotation <= -180.0f -> 360 + rotation
            else -> rotation
        }

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
    }
}