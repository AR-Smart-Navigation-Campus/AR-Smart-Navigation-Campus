package com.example.locations.Admin.all_location

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.Admin.all_location.single_location.LocationUpdatesLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 *AdminViewModel class that extends AndroidViewModel and provides LiveData objects for the UI to observe.
 */
class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore // Firestore instance

    private val _locationData = MutableLiveData<List<LocationData>>() // MutableLiveData object for the list of LocationData objects
    val locationData: LiveData<List<LocationData>> get() = _locationData // LiveData object for the list of LocationData objects

    private val _chosenItem = MutableLiveData<LocationData>() // MutableLiveData object for the selected LocationData object
    val chosenItem: LiveData<LocationData> get() = _chosenItem // LiveData object for the selected LocationData object


    init {
        fetchLocations()
    }

    //Adds a new entry to Firestore
    fun addEntry(imageUri: String) {
        //Generate a unique id
        val uniqueId = System.currentTimeMillis()
        val newEntry = LocationData(id = uniqueId ,location = location.value!!, name = userInput.value!!, azimuth = _azimuth.value.toString(), imgUrl = imageUri)
        saveBuildingToFirestore(newEntry)

    }

    // deletes an entry from Firestore
    fun deleteEntry(location: LocationData) {
        deleteBuildingFromFirestore(location.id)
    }



    //Sets the selected LocationData object
    fun setLocation(it: LocationData) {
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

//////////////////////////////////////////////////////////////////////////////
    // Fetches the locations from Firestore
    fun fetchLocations() {
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

    // Saves a building to Firestore
    private fun saveBuildingToFirestore(building: LocationData) {
        val buildingData = hashMapOf(
            "id" to building.id,
            "name" to building.name,
            "location" to building.location,
            "azimuth" to building.azimuth,
            "imgUrl" to building.imgUrl
        )

        db.collection("buildings")
            .add(buildingData)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    // Deletes a building from Firestore
    private fun deleteBuildingFromFirestore(id:Long) {
        db.collection("buildings").get().addOnSuccessListener { result ->
            for (document in result) {
                if (document.data["id"] == id) {
                    db.collection("buildings").document(document.id).delete()
                    Log.d("Firestore", "DocumentSnapshot successfully deleted!")
                }
            }
        }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting document", e)
            }
    }

    //////////////////////////////////////////////////////////////////////////////
}