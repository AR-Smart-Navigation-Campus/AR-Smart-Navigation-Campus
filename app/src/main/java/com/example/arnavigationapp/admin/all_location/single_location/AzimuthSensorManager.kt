package com.example.arnavigationapp.admin.all_location.single_location

import android.content.Context
import android.hardware.Sensor
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
    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Accelerometer sensor
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    // Magnetic field sensor
    private var magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Array to hold gravity values
    private var gravity: FloatArray? = null
    // Array to hold geomagnetic values
    private var geomagnetic: FloatArray? = null

    private val sensorEventListener = object : SensorEventListener {
        // SensorEventListener to listen for sensor events
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} // Not in use
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                // If the event is from the accelerometer, update gravity values
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
                    gravity = event.values
                // If the event is from the magnetic field sensor, update geomagnetic values
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
                    geomagnetic = event.values
                // If both gravity and geomagnetic values are available, calculate azimuth
                if (gravity != null && geomagnetic != null) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    val success = SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)
                    if (success) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        onAzimuthChanged(azimuth) // Call the onAzimuthChanged function with the calculated azimuth
                    }
                }
            }
        }
    }

    // Start listening to sensor events
    fun startListening() {
        // Register sensor event listener for accelerometer and magnetometer
        accelerometer?.also { accelerometer ->
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        // Register sensor event listener for magnetometer
        magnetometer?.also { magnetometer ->
            sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Stop listening to sensor events
    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
    }
}