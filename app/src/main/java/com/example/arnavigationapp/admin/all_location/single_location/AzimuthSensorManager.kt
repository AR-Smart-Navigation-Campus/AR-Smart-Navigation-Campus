package com.example.arnavigationapp.admin.all_location.single_location

import android.content.Context
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ROTATION_VECTOR
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * AzimuthSensorManager class to manage sensor events for azimuth updates.
 * @param context Context of the application
 * @param onAzimuthChanged Function to be called when azimuth changes
 */

class AzimuthSensorManager(context: Context, private val onAzimuthChanged: (Float) -> Unit) {

    // SensorManager to access sensors
    private var sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Accelerometer sensor
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Magnetic field sensor
    private var magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Array to hold gravity values
    private var gravity: FloatArray? = null

    // Array to hold geomagnetic values
    private var geomagnetic: FloatArray? = null

    // Rotation Vector sensor
    private var rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)

    private val sensorEventListener  = object : SensorEventListener {
        // SensorEventListener to listen for sensor events
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} // Not in use
        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return

            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
                Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
            }

            if (gravity!= null && geomagnetic != null) {
                val rotationMatrix = FloatArray(9)
                val inclinationMatrix = FloatArray(9)

                val success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)
                if (success) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    onAzimuthChanged(azimuth)
                }
            }
        }
    }


    private val rotationVectorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} // Not in use

        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return

            if (event.sensor.type == TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                onAzimuthChanged(azimuth)
            }
        }
    }

    // Start listening to sensor events
    fun startListening() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                rotationVectorEventListener,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }else {

            // Check for sensor availability
            if (accelerometer == null || magnetometer == null) {
                Log.w("AzimuthSensorManager", "Required sensors are not available on this device.")
                return
            }

            // Register sensor event listener for accelerometer and magnetometer
            accelerometer?.also { accelerometer ->
                sensorManager.registerListener(
                    sensorEventListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            // Register sensor event listener for magnetometer
            magnetometer?.also { magnetometer ->
                sensorManager.registerListener(
                    sensorEventListener,
                    magnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
    }

    // Stop listening to sensor events
    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
        sensorManager.unregisterListener(rotationVectorEventListener)
    }
}