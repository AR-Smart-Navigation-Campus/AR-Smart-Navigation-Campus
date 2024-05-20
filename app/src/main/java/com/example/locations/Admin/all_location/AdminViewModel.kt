package com.example.locations.Admin.all_location

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.Admin.all_location.single_location.AddLocationFragment
import com.example.locations.Admin.all_location.single_location.LocationUpdatesLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

// AdminViewModel class that extends AndroidViewModel and provides LiveData objects for the UI to observe.
class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore

    private val _locationData = MutableLiveData<List<LocationData>>()
    val locationData: LiveData<List<LocationData>> get() = _locationData

    private val _chosenItem = MutableLiveData<LocationData>()
    val chosenItem: LiveData<LocationData> get() = _chosenItem

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        db.collection("buildings").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.d("Firestore", "Error getting documents: ", error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val locations = snapshot.toObjects(LocationData::class.java)
                Log.d("Firestore", "Locations: $locations")
                _locationData.postValue(locations)
            } else {
                Log.d("Firestore", "No locations found")
            }
        }
    }



    // Adds a new entry to the database and JSON file
    fun addEntry(imageUri: String) {

//        // Generate a unique id
        val uniqueId = System.currentTimeMillis()
      val newEntry = LocationData(id = uniqueId ,location = location.value!!, name = userInput.value!!, azimuth = _azimuth.value.toString(), imgUrl = imageUri)
        AddLocationFragment().saveBuildingToFirestore(newEntry)


    }



<<<<<<< HEAD
        // Initialize an empty list of LocationData
        val existingEntries: MutableList<LocationData> = mutableListOf()

        // Check if the file exists
        if (file.exists()) {
            // Read the existing JSON string from the file
            val gson = Gson()
            val existingJson = FileReader(file).readText()

            // Convert the existing JSON string to a list of LocationData objects
            val type = object : TypeToken<List<LocationData>>() {}.type
            existingEntries.addAll(gson.fromJson(existingJson, type))

            // Find the entry with the same id and remove it from the list
            val iterator = existingEntries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.id == locationData.id) {
                    iterator.remove()
                    break
                }
            }

            // If the list is empty after removal, delete the file
            if (existingEntries.isEmpty()) {
                file.delete()
            } else {
                // Convert the updated list back to a JSON string
                val updatedJson = gson.toJson(existingEntries)

                // Write the updated JSON string back to the file
                FileWriter(file).use { it.write(updatedJson) }
            }
        }
    }

    // Sets the selected LocationData object
    fun setLocation(it: LocationData) {
=======
//    // Sets the selected LocationData object
   fun setLocation(it: LocationData) {
>>>>>>> 1156cdd2e2dc05668cb295d6f9539a3b076663f0
        _chosenItem.value = it
    }


////////////////////////////////////////////////////////////////////////////

    // LiveData object for the current address
    val address: LiveData<String> = LocationUpdatesLiveData(application.applicationContext)
    // LiveData object for the current location
    private val _location = MutableLiveData<String>()
    val location: LiveData<String> = _location
    // LiveData object for the user input
    private val _userInput = MutableLiveData<String>()
    val userInput: LiveData<String> = _userInput

    // Updates the user input with the provided text
    fun addUserInput(text: String) {
        _userInput.value = text
    }

    // Updates the current location with the provided value
    fun updateLocation(location: String) {
        _location.value = location
    }

//////////////////////////////////////////////////////////////////////////////


// Private mutable LiveData for storing the current azimuth (exposed as read-only)
private val _azimuth = MutableLiveData<Float>()
    val azimuth: LiveData<Float> = _azimuth

    // Updates the current azimuth with the provided value
    fun updateAzimuth(azimuth: Float) {
        _azimuth.value = azimuth
    }

}
