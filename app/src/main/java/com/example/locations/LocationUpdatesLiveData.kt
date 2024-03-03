package com.example.locations

import android.content.Context
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit

class LocationUpdatesLiveData(context:Context) : LiveData<String>() {

    private val locationClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

//    private val geocoder by lazy {
//        Geocoder(context)
//    }

    //private val job = Job()

    //private val scope = CoroutineScope(job + Dispatchers.IO)

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,TimeUnit.SECONDS.toMillis(20)).build()


    private val locationCallback = object  : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            p0.lastLocation?.let {
//                scope.launch {
//                    val addresses = geocoder.getFromLocation(it.latitude , it.longitude , 1)
//                    postValue(addresses?.get(0)?.getAddressLine(0))
//                }
                 MainActivityViewModel.data.latitude = it.latitude
                MainActivityViewModel.data.longitude = it.longitude
                postValue("${it.latitude} , ${it.longitude}")
            }
        }
    }

    override fun onActive() {
        super.onActive()
        try {
            locationClient.requestLocationUpdates(locationRequest , locationCallback , Looper.getMainLooper())
        }catch (e:SecurityException){
            Log.d("LocationUpdatesLiveData" , "Missing location permission")
        }

    }

    override fun onInactive() {
        super.onInactive()
        locationClient.removeLocationUpdates(locationCallback)
    }
}


