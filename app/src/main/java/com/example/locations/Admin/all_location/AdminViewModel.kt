package com.example.locations.Admin.all_location

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.Admin.all_location.single_location.LocationUpdatesLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale
import java.util.UUID

/**
 * AdminViewModel class that extends AndroidViewModel and provides LiveData objects for the UI to observe.
 * Provide functions to add and delete entries from Firestore.
 * Provide functions to set the selected LocationData object and update the user input, location, and azimuth.
 * Fetches the locations from Firestore and updates the locationData LiveData object.
 * @param application: Application
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
    fun addEntry(imageUri: Uri) {
        Log.d("Uri", "$imageUri")
        uploadImageToFirebaseStorage(imageUri) { imageUrl ->
            // Generate a unique id
            val uniqueId = System.currentTimeMillis()
            val newEntry = LocationData(
                id = uniqueId,
                location = location.value!!,
                name = userInput.value!!,
                azimuth = azimuth.value.toString(),
                imgUrl = imageUrl
            )
            saveBuildingToFirestore(newEntry)
        }
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

    val address: LiveData<String> = LocationUpdatesLiveData(application.applicationContext)  // LiveData object for the current address

    private val _location = MutableLiveData<String>() // MutableLiveData object for the current location
    val location: LiveData<String> = _location // LiveData object for the current location

    private val _userInput = MutableLiveData<String>() // MutableLiveData object for the user input
    val userInput: LiveData<String> = _userInput // LiveData object for the user input

    // Updates the user input with the provided text
    fun addUserInput(text: String) {
        _userInput.value = text
    }

    // Updates the current location with the provided value
    fun updateLocation(location: String) {
        _location.value = location
    }

//////////////////////////////////////////////////////////////////////////////

    private val _azimuth = MutableLiveData<Float>() // MutableLiveData object for the current azimuth
    val azimuth: LiveData<Float> = _azimuth // LiveData object for the current azimuth

    // Updates the current azimuth with the provided value
    fun updateAzimuth(azimuth: Float) {
        _azimuth.value = azimuth
    }

//////////////////////////////////////////////////////////////////////////////
    // Fetches the locations from Firestore
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

    // Uploads an image to Firebase Storage and gets the URL
    private fun uploadImageToFirebaseStorage(imageUri: Uri, callback: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting download URL: ${exception.message}", exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error uploading image to Firebase Storage: ${exception.message}", exception)
            }
    }
    //////////////////////////////////////////////////////////////////////////////
}