package com.example.locations.Admin.all_location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.Admin.all_location.model.LocationRepository
import com.example.locations.Admin.all_location.single_location.LocationUpdatesLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

// AdminViewModel class that extends AndroidViewModel and provides LiveData objects for the UI to observe.
class AdminViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData object for the list of LocationData objects
    private val _chosenItem  = MutableLiveData<LocationData>()
    // LiveData object for the selected LocationData object
    val chosenItem : LiveData<LocationData> get() = _chosenItem
    // LocationRepository object to interact with the database
    private val repository = LocationRepository(application)
    // LiveData object for the list of LocationData objects
    val locationData : LiveData<List<LocationData>>? = repository.getLocations()


    // Adds a new entry to the database and JSON file
    fun addEntry(imageUri: String) {

        // Generate a unique id
        val uniqueId = System.currentTimeMillis()

        val newEntry = LocationData(id = uniqueId ,location = location.value!!, text = userInput.value!!, azimuth = _azimuth.value.toString(), img = imageUri)
        repository.addLocation(newEntry)

        // Define the file
        val file = File(getApplication<Application>().filesDir, "locationData.json")

        // Initialize an empty list of LocationData
        val gson = Gson()
        val existingEntries: MutableList<LocationData> = mutableListOf()

        // Check if the file exists
        if (file.exists()) {
            // Read the existing JSON string from the file
            val existingJson = FileReader(file).readText()

            // Convert the existing JSON string to a list of LocationData objects
            val type = object : TypeToken<List<LocationData>>() {}.type
            existingEntries.addAll(gson.fromJson(existingJson, type))
        }

        // Add the new entry to the list
        existingEntries.add(newEntry)

        // Convert the updated list back to a JSON string
        val updatedJson = gson.toJson(existingEntries)

        // Write the updated JSON string back to the file
        FileWriter(file).use { it.write(updatedJson) }
    }

    // Deletes an entry from the database and JSON file
    fun deleteEntry(locationData: LocationData){
        repository.deleteLocation(locationData)

        // Define the file
        val file = File(getApplication<Application>().filesDir, "locationData.json")

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
