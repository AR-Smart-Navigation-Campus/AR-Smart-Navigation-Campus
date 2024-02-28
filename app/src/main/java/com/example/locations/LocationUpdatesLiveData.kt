package com.example.locations

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.MainScope
import java.util.concurrent.TimeUnit

class LocationUpdatesLiveData(context:Context) : LiveData<String>() {

    private val locationClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,TimeUnit.SECONDS.toMillis(20))

    private val locationCallback = object  : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            p0.lastLocation?.let {
                postValue("${it.latitude} , ${it.longitude}")
            }

        }
    }

    override fun onActive() {
        super.onActive()
        try {
            locationClient.requestLocationUpdates(locationRequest,locationCallback , Looper.getMainLooper())
        }catch (e:SecurityException){
            Log.d("LocationUpdatesLiveData" , "Missing location permission")
        }

    }

    override fun onInactive() {
        super.onInactive()
        locationClient.removeLocationUpdates(locationCallback)
    }
}


