package com.example.locations.Admin.all_location

import android.app.Application
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.Admin.all_location.single_location.LocationUpdatesLiveData
import com.example.locations.R
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
                description = "No description",
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

    // Create a Location object from a string
    fun createLocation(location:String): Location {
        val coordinates = location.split(",") // Split the string by comma
        Location("provider").apply {
            latitude = coordinates[0].toDouble() // Set the latitude
            longitude = coordinates[1].toDouble() // Set the longitude
            return this
        }
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
            "description" to building.description,
            "imgUrl" to building.imgUrl
        )

        db.collection("buildings")
            .add(buildingData)
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
    fun getLocationDescriptionResId(locationDescirption: String): Int {
        return when (locationDescirption) {
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