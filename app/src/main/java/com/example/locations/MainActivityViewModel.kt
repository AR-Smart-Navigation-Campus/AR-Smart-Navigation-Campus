package com.example.locations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application){

    val address: LiveData<String> = LocationUpdatesLiveData(application.applicationContext)

     object data{
        var latitude : Double = 0.0
        var longitude: Double = 0.0
        var name:String = ""
    }

    val placeData : MutableLiveData<MutableList<data>> = MutableLiveData(ArrayList())

    fun addData(latitude : Double , longitude : Double , name:String){
        data.name = name
        data.latitude = latitude
        data.longitude = longitude
        placeData.value?.add(data)
    }
}