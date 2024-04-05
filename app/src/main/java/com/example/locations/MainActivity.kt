package com.example.locations

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.locations.databinding.ActivityMainBinding

/**
 * Main activity for the location data app.
 */
class MainActivity : AppCompatActivity() {

    // Binding object instance with access to the views in the layout.
    private lateinit var binding: ActivityMainBinding

    // Instance of AzimuthSensorManager to get azimuth updates.
    private lateinit var azimuthSensorManager: AzimuthSensorManager

    //Reference to the MainActivityViewModel obtained using viewModels() delegate.
    private val viewModel: MainActivityViewModel by viewModels()

    //ActivityResultLauncher for requesting location permission.
    private val locationRequestLauncher : ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        // If permission is granted, start getting location updates.
        if(it){
            getLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this activity.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check for location permission and request if necessary
        if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocationUpdates()
        }
        else{
            // Request location permission.
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Initialize AzimuthSensorManager and set azimuth update listener.
        azimuthSensorManager = AzimuthSensorManager(this) { azimuth ->
            viewModel.updateAzimuth(azimuth)
        }

        // Set click listener for the save button
        binding.btnSavePlace.setOnClickListener {
            // Get user input and current location.
            val text = binding.placeName.text.toString()
            val location = binding.textView.text.toString()

            // Update location and user input in the view model
            viewModel.updateLocation(location)
            viewModel.addUserInput(text)

            // Add a new entry with the updated data
            viewModel.addEntry()

            // Clear the input field
            binding.placeName.text.clear()

            // Update UI with the latest entry (if any)
            val latestEntry = viewModel.locationDataList.value.lastOrNull()
            if (latestEntry != null) {
                binding.nameView.text = latestEntry.text
                binding.coordinatesView.text = latestEntry.location
                binding.azimuthView.text = latestEntry.azimuth
            }
        }


        // Observe user input from the view model (currently empty)
        viewModel.userInput.observe(this) { text ->

        }

        // Observe location data from the view model (currently empty)
        viewModel.location.observe(this) { cord ->

        }

    }

    override fun onResume() {
        super.onResume()
        // Start listening to azimuth updates when the activity is resumed.
        azimuthSensorManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        // Stop listening to azimuth updates when the activity is paused.
        azimuthSensorManager.stopListening()
    }

    //Fetches location updates and updates the UI text view.
    private fun getLocationUpdates(){
        // Observe address updates from the view model and update the UI.
        viewModel.address.observe(this){
            binding.textView.text = it
        }
    }
}