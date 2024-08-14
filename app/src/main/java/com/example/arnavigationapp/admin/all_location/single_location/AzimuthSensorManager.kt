package com.example.arnavigationapp.admin.all_location.single_location

import android.content.Context
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ROTATION_VECTOR
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * AzimuthSensorManager class to manage sensor events for azimuth updates.
 * @param context Context of the application
 * @param onAzimuthChanged Function to be called when azimuth changes
 */

class AzimuthSensorManager(context: Context, private val onAzimuthChanged: (Float) -> Unit) {

    // SensorManager to access sensors
    private var sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Rotation Vector sensor
    private var rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)

    private val rotationVectorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} // Not in use

        // Called when sensor values change
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
        }
    }

        // Stop listening to sensor events
        fun stopListening() {
            sensorManager.unregisterListener(rotationVectorEventListener)
        }

}