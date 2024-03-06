package com.example.locations

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.locations.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * Main activity for the location data app.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //Reference to the MainActivityViewModel obtained using viewModels() delegate.
    private val viewModel: MainActivityViewModel by viewModels()

    //ActivityResultLauncher for requesting location permission.
    private val locationRequestLauncher : ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){
            getLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //// Check for location permission and request if necessary
        if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocationUpdates()
        }
        else{
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Set click listener for the save button
        binding.btnSavePlace.setOnClickListener {
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
            }
        }

          // Removed commented-out code for demonstration purposes
//        lifecycleScope.launch {
//            viewModel.locationDataList.collect { data ->
//                if (data.isNotEmpty()) {
//                    binding.nameView.text = data.last().text
//                    binding.coordinatesView.text = data.last().location
//                }
//            }
//        }

        // Observe user input from the view model (currently empty)
        viewModel.userInput.observe(this) { text ->

        }

        // Observe location data from the view model (currently empty)
        viewModel.location.observe(this) { cord ->

        }

    }

    //Fetches location updates and updates the UI text view.
    private fun getLocationUpdates(){
        viewModel.address.observe(this){
            binding.textView.text = it
        }
    }
}