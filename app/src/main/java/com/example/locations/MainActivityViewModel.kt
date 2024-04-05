package com.example.locations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Main view model for the application, managing location and user input data
class MainActivityViewModel(application: Application) : AndroidViewModel(application){

    // LiveData representing location updates, retrieved from LocationUpdatesLiveData
    val address: LiveData<String> = LocationUpdatesLiveData(application.applicationContext)

    // Private mutable LiveData for storing the current location (exposed as read-only)
    private val _location = MutableLiveData<String>()
    val location: LiveData<String> = _location

    // Private mutable LiveData for storing the current azimuth (exposed as read-only)
    private val _azimuth = MutableLiveData<Float>()
    val azimuth: LiveData<Float> = _azimuth

    // Private mutable list to store LocationData entries
    private val _locationDataList = mutableListOf<LocationData>()
    // MutableStateFlow representing the list of LocationData, initialized with the mutable list
    val locationDataList = MutableStateFlow<List<LocationData>>(_locationDataList)

    // Private mutable LiveData for storing user input (exposed as read-only)
    private val _userInput = MutableLiveData<String>()
    val userInput: LiveData<String> = _userInput


    ////////////////////////////////////////////////////////////////////
    // Room database instance
    private val database = Room.databaseBuilder(
        application.applicationContext,
        LocationDatabase::class.java, "database-name"
    ).build()

    ////////////////////////////////////////////////////////////////////



    // Updates the current location with the provided value
    fun updateLocation(location: String) {
        _location.value = location
    }

    // Updates the current azimuth with the provided value
    fun updateAzimuth(azimuth: Float) {
        _azimuth.value = azimuth
    }

    // Updates the user input with the provided text
    fun addUserInput(text: String) {
        _userInput.value = text
    }

    // Adds a new LocationData entry to the list, combining current location and user input
    fun addEntry() {
        val location = _location.value!! // Force unwrapping, ensure data is present
        val text = _userInput.value!! // Force unwrapping, ensure data is present
        val azimuth = _azimuth.value.toString() // Convert azimuth to string
        val newEntry = LocationData(location = location, text = text, azimuth = azimuth)
        _locationDataList.add(newEntry) // Updates the underlying list, triggering StateFlow emission

        // Insert the new entry into the database
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.locationDao().insertLocation(newEntry)
            }
        }
    }
}