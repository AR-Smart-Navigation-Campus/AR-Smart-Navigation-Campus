//package com.example.locations
//
//import android.app.Application
//import android.content.Context
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//
//class AzimuthViewModel(application: Application) : AndroidViewModel(application) {
//    private val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
//    private val _azimuth = MutableLiveData<Float>()
//    val azimuth: LiveData<Float> get() = _azimuth
//
//    private val sensorEventListener = object : SensorEventListener {
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//        override fun onSensorChanged(event: SensorEvent?) {
//            if (event?.sensor == rotationSensor) {
//                val rotationMatrix = FloatArray(9)
//                if (event != null) {
//                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
//                }
//                val orientation = FloatArray(3)
//                SensorManager.getOrientation(rotationMatrix, orientation)
//                val azimuthInRadians = orientation[0]
//                val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
//                _azimuth.value = azimuthInDegrees
//            }
//        }
//    }
//
//    fun updateAzimuth(azimuth: Float) {
//        _azimuth.value = azimuth
//    }
//
//    fun startListening() {
//        sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL)
//    }
//
//    fun stopListening() {
//        sensorManager.unregisterListener(sensorEventListener)
//    }
//}
