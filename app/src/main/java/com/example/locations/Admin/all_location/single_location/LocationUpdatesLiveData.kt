package com.example.locations.Admin.all_location.single_location

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.locations.databinding.AddLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit

/**
 * LiveData class that provides updates for the user's location.
 * This class handles requesting location updates from the FusedLocationProviderClient
 * and emits updates to observers with the latest location as a formatted string.
 */
class LocationUpdatesLiveData(context:Context) : LiveData<String>() {
    //FusedLocationProviderClient instance to request location updates.
    private val locationClient : FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Location request object specifying update interval and priority.
    private val locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,TimeUnit.SECONDS.toMillis(1)).build()

    //Location callback to receive location updates from the FusedLocationProviderClient.
    private val locationCallback = object  : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            p0.lastLocation?.let {
                postValue("${it.latitude} , ${it.longitude} , ${it.accuracy}")
            }
        }
    }

    //Called when this LiveData becomes active. Starts requesting location updates.
    override fun onActive() {
        super.onActive()
        try {
            locationClient.requestLocationUpdates(locationRequest , locationCallback , Looper.getMainLooper())
        }catch (e:SecurityException){
            Log.d("LocationUpdatesLiveData" , "Missing location permission")
        }
    }

    //Called when this LiveData becomes inactive. Stops requesting location updates.
    override fun onInactive() {
        super.onInactive()
        locationClient.removeLocationUpdates(locationCallback)
    }
}


