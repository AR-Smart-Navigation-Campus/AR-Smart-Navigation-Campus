//package com.example.locations
//
//import android.annotation.SuppressLint
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.viewModelScope
//import androidx.room.Room
//import com.example.locations.data.model.LocationDatabase
//import com.example.locations.data.model.LocationData
//import com.example.locations.single_location.LocationUpdatesLiveData
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class LocationsViewModel(application: Application) : AndroidViewModel(application) {
//
//    val address: LiveData<String> = LocationUpdatesLiveData(application.applicationContext)
//    private val _location = MutableLiveData<String>()
//    val location: LiveData<String> = _location
//    private val _userInput = MutableLiveData<String>()
//    val userInput: LiveData<String> = _userInput
////    private val _chosenItem  = MutableLiveData<LocationData>()
////    val chosenItem : LiveData<LocationData> get() = _chosenItem
//
////    private val database = Room.databaseBuilder(
////        application.applicationContext,
////        LocationDatabase::class.java, "database-name"
////    ).build()
////
////    init {
////        loadData()
////    }
////
////     fun loadData() {
////        viewModelScope.launch {
////            val data = withContext(Dispatchers.IO) { database.locationDao().getAll() }
////            _locationDataList.addAll(data)
////        }
////    }
//
//    fun updateLocation(location: String) {
//        _location.value = location
//    }
//
//    fun addUserInput(text: String) {
//        _userInput.value = text
//    }
//
////    fun addEntry() {
////        val newEntry = LocationData(location = _location.value!!, text = _userInput.value!!, azimuth = azimuthViewModel.azimuth.value.toString(), img = R.drawable.doorms)
////        _locationDataList.add(newEntry)
////        viewModelScope.launch {
////            withContext(Dispatchers.IO) {
////                database.locationDao().insertLocation(newEntry)
////            }
////        }
////    }
//
////    fun deleteEntry(locationData: LocationData){
////        _locationDataList.remove(locationData)
////        viewModelScope.launch {
////            withContext(Dispatchers.IO) {
////                database.locationDao().deleteLocation(locationData)
////            }
////        }
////    }
////
////    fun setLocation(it: LocationData) {
////        _chosenItem.value = it
////    }
//}
//
//
////package com.example.locations
////
////import android.app.Application
////import android.content.Context
////import android.net.Uri
////import androidx.lifecycle.AndroidViewModel
////import androidx.lifecycle.LiveData
////import androidx.lifecycle.MutableLiveData
////import androidx.lifecycle.viewModelScope
////import androidx.room.Room
////import com.example.locations.data.model.LocationDao
////import com.example.locations.data.model.LocationData
////import com.example.locations.data.model.LocationDatabase
////import com.example.locations.single_location.LocationUpdatesLiveData
////import com.google.gson.Gson
////import com.google.gson.reflect.TypeToken
////import kotlinx.coroutines.Dispatchers
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.launch
////import kotlinx.coroutines.withContext
////
////// Main view model for the application, managing location and user input data
////class LocationsViewModel(application: Application) : AndroidViewModel(application){
////
////    // LiveData representing location updates, retrieved from LocationUpdatesLiveData
////    val address: LiveData<String> = LocationUpdatesLiveData(application.applicationContext)
////
////    // Private mutable LiveData for storing the current location (exposed as read-only)
////    private val _location = MutableLiveData<String>()
////    val location: LiveData<String> = _location
////
////    // Private mutable LiveData for storing the current azimuth (exposed as read-only)
////    private val _azimuth = MutableLiveData<Float>()
////    val azimuth: LiveData<Float> = _azimuth
////
////    // Private mutable list to store LocationData entries
////    private val _locationDataList = mutableListOf<LocationData>()
////    // MutableStateFlow representing the list of LocationData, initialized with the mutable list
////    val locationDataList = MutableStateFlow<List<LocationData>>(_locationDataList)
////
////    // Private mutable LiveData for storing user input (exposed as read-only)
////    private val _userInput = MutableLiveData<String>()
////    val userInput: LiveData<String> = _userInput
////
////    private val _chosenItem  = MutableLiveData<LocationData>()
////    val chosenItem : LiveData<LocationData> get() = _chosenItem
////
////    init {
////        loadData()
////    }
////
////    ////////////////////////////////////////////////////////////////////
////    // Room database instance
////    private val database = Room.databaseBuilder(
////        application.applicationContext,
////        LocationDatabase::class.java, "database-name"
////    ).build()
////
////    fun saveData() {
////        val sharedPreferences = getApplication<Application>().getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
////        val editor = sharedPreferences.edit()
////        val gson = Gson()
////        val json = gson.toJson(_locationDataList)
////        editor.putString("location list", json)
////        editor.apply()
////    }
////
////    private fun loadData() {
////        val sharedPreferences = getApplication<Application>().getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
////        val gson = Gson()
////        val json = sharedPreferences.getString("location list", null)
////        val type = object : TypeToken<List<LocationData>>() {}.type
////        val data: List<LocationData>? = gson.fromJson(json, type)
////
////        if (data != null) {
////            _locationDataList.addAll(data)
////        }
////    }
////
////
////    ////////////////////////////////////////////////////////////////////
////
////
////
////    // Updates the current location with the provided value
////    fun updateLocation(location: String) {
////        _location.value = location
////    }
////
////    // Updates the current azimuth with the provided value
////    fun updateAzimuth(azimuth: Float) {
////        _azimuth.value = azimuth
////    }
////
////    // Updates the user input with the provided text
////    fun addUserInput(text: String) {
////        _userInput.value = text
////    }
////
////    // Adds a new LocationData entry to the list, combining current location and user input
////    fun addEntry() {
////        val location = _location.value!! // Force unwrapping, ensure data is present
////        val text = _userInput.value!! // Force unwrapping, ensure data is present
////        val azimuth = _azimuth.value.toString() // Convert azimuth to string
////        val newEntry = LocationData(location = location, text = text, azimuth = azimuth , img = R.drawable.doorms)
////        _locationDataList.add(newEntry) // Updates the underlying list, triggering StateFlow emission
////        saveData()
////
////        // Insert the new entry into the database
////        viewModelScope.launch {
////            withContext(Dispatchers.IO) {
////                database.locationDao().insertLocation(newEntry)
////            }
////        }
////    }
////
////    fun deleteEntry(locationData: LocationData){
////        _locationDataList.remove(locationData)
////        viewModelScope.launch {
////            withContext(Dispatchers.IO) {
////                database.locationDao().deleteLocation(locationData)
////            }
////            val sharedPreferences = getApplication<Application>().getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
////            val editor = sharedPreferences.edit()
////            editor.remove(locationData.text)
////            editor.apply()
////        }
////        saveData()
////    }
////
////    override fun onCleared() {
////        super.onCleared()
////        saveData()
////    }
////
////    fun setLocation(it: LocationData) {
////        _chosenItem.value = it
////    }
////}