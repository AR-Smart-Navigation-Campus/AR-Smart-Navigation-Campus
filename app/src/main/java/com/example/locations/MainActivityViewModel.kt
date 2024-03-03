package com.example.locations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application){

    val address: LiveData<String> = LocationUpdatesLiveData(application.applicationContext)

}