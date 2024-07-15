package com.example.locations.admin.all_location

import android.app.Application
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.locations.admin.all_location.model.FirestoreRepository
import com.example.locations.admin.all_location.model.LocationData
import com.example.locations.admin.all_location.single_location.LocationUpdatesLiveData
import com.example.locations.R

/**
 * AdminViewModel class that extends AndroidViewModel and provides LiveData objects for the UI to observe.
 * Provide functions to add and delete entries from Firestore.
 * Provide functions to set the selected LocationData object and update the user input, location, and azimuth.
 * Fetches the locations from Firestore and updates the locationData LiveData object.
 * @param application: Application
 */

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    val admin = "navigationproject2024@gmail.com"

    private val repository = FirestoreRepository() // FirestoreRepository object

    val address: LiveData<String> = LocationUpdatesLiveData(application.applicationContext)  // LiveData object for the current address

    private val _location = MutableLiveData<String>() // MutableLiveData object for the current location
    val location: LiveData<String> = _location // LiveData object for the current location

    private val _userInput = MutableLiveData<String>() // MutableLiveData object for the user input
    val userInput: LiveData<String> = _userInput // LiveData object for the user input

    private val _azimuth = MutableLiveData<Float>() // MutableLiveData object for the current azimuth
    val azimuth: LiveData<Float> = _azimuth // LiveData object for the current azimuth

    private val _locationData = MutableLiveData<List<LocationData>>() // MutableLiveData object for the list of LocationData objects
    val locationData: LiveData<List<LocationData>> get() = _locationData // LiveData object for the list of LocationData objects

    private val _chosenItem = MutableLiveData<LocationData>() // MutableLiveData object for the selected LocationData object
    val chosenItem: LiveData<LocationData> get() = _chosenItem // LiveData object for the selected LocationData object

    init {
        fetchLocations()
    }

    // Fetches the locations from Firestore
    private fun fetchLocations() {
        repository.fetchLocations { result ->
            result.onSuccess { locations -> _locationData.postValue(locations) }
                .onFailure { exception ->
                    Log.e(
                        "ViewModel",
                        "Error fetching locations",
                        exception
                    )
                }
        }
    }


    //Adds a new entry to Firestore
    fun addEntry(imageUri: Uri) {
        Log.d("Uri", "$imageUri")
        repository.uploadImageToFirebaseStorage(imageUri) { imageUrl ->
            // Generate a unique id
            val uniqueId = System.currentTimeMillis()
            val newEntry = LocationData(
                id = uniqueId,
                location = location.value!!,
                name = userInput.value!!,
                azimuth = azimuth.value.toString(),
                description = "No description",
                imgUrl = imageUrl
            )
            repository.saveBuildingToFirestore(newEntry)
        }
    }

    // deletes an entry from Firestore
    fun deleteEntry(location: LocationData) {
        repository.deleteBuildingFromFirestore(location.id)
    }


    //Sets the selected LocationData object
    fun setLocation(it: LocationData) {
        _chosenItem.value = it
    }

    // Updates the user input with the provided text
    fun addUserInput(text: String) {
        _userInput.value = text
    }

    // Updates the current location with the provided value
    fun updateLocation(location: String) {
        _location.value = location
    }

    // Create a Location object from a string
    fun createLocation(location: String): Location {
        val coordinates = location.split(",") // Split the string by comma
        Location("provider").apply {
            latitude = coordinates[0].toDouble() // Set the latitude
            longitude = coordinates[1].toDouble() // Set the longitude
            return this
        }
    }

    // Updates the current azimuth with the provided value
    fun updateAzimuth(azimuth: Float) {
        _azimuth.value = azimuth
    }


    // Method to map location names to string resource IDs
    fun getLocationNameResId(locationName: String): Int {
        return when (locationName) {
            "Cafeteria" -> R.string.cafeteria
            "Fichman Gate" -> R.string.fichman_gate
            "Hoffin Gate" -> R.string.hoffin_gate
            "Golomb Gate" -> R.string.golomb_gate
            "Building 1" -> R.string.building_1
            "Building 2" -> R.string.building_2
            "Building 3-A" -> R.string.building_3_A
            "Building 3-B" -> R.string.building_3_B
            "Building 4-Workshop" -> R.string.building_4_workshop
            "Building 5" -> R.string.building_5
            "Building 6" -> R.string.building_6
            "Building 7" -> R.string.building_7
            "Building 8-A" -> R.string.building_8_A
            "Building 8-B" -> R.string.building_8_B
            "Building 8-C" -> R.string.building_8_C
            "Aguda" -> R.string.aguda
            "Club" -> R.string.club
            "Materials Shop" -> R.string.materials_shop
            "Dorms" -> R.string.dorms
            "Dorms Gate" -> R.string.dorms_gate
            "Library" -> R.string.library
            else -> R.string.unknown_location
        }
    }

    // Method to map location names to string resource IDs
    fun getLocationDescriptionResId(locationDescription: String): Int {
        return when (locationDescription) {
            "A student\\'s club in building 6" -> R.string.club_desc
            "The student\\'s union in building 5" -> R.string.aguda_desc
            "The first entrance to building 8" -> R.string.building_8_a_desc
            "The second entrance to building 8" -> R.string.building_8_b_desc
            "The third entrance to building 8" -> R.string.building_8_c_desc
            "The first entrance to building 3" -> R.string.building_3_a_desc
            "The second entrance to building 3" -> R.string.building_3_b_desc
            "Student\\'s dorms near the parking lot and building 7" -> R.string.dorms_desc
            "The closest gate to the dorms" -> R.string.dorms_gate_desc
            "Top floor of building 5" -> R.string.library_desc
            "The main entrance to HIT , in front of building 5" -> R.string.fichman_gate_desc
            "A store near building 6 (floor -1)" -> R.string.store_desc
            "A gate near building 8 and a parking lot" -> R.string.hoffin_gate_desc
            "The rear gate of Hit , near building 1 and 2" -> R.string.golomb_gate_desc
            "The main building in HIT" -> R.string.building_5_desc
            else -> R.string.no_desc
        }
    }
}