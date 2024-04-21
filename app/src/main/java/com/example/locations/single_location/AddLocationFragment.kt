package com.example.locations.single_location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.locations.MainViewModel
import com.example.locations.R
import com.example.locations.databinding.AddLocationBinding

// AddLocationFragment is a Fragment that allows the user to add a new location entry.
class AddLocationFragment: Fragment() {
    // View binding
    private var _binding: AddLocationBinding? = null
    private val binding get() = _binding!!
    // Uri of the image selected by the user.
    private var imageUri: Uri? = null

    // Instance of MainViewModel to access the data.
    private val viewModel: MainViewModel by activityViewModels()

    // Instance of AzimuthSensorManager to get azimuth updates.
    private lateinit var azimuthSensorManager: AzimuthSensorManager

    // Register a launcher for picking an image.
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            handleImagePick(it)
        }
    }
    // Register a launcher for requesting location permission.
    private val locationRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { handleLocationPermissionResult(it) }

    // Create the view.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AddLocationBinding.inflate(inflater, container, false)
        // Initialize AzimuthSensorManager and set azimuth update listener.
        azimuthSensorManager = AzimuthSensorManager(requireContext()) { azimuth ->
            viewModel.updateAzimuth(azimuth)
        }
        setupView()
        return binding.root
    }

    // Set up the view.
    private fun setupView() {
        checkAndRequestLocationPermission()
        binding.btnSavePlace.setOnClickListener { handleSavePlaceClick() }
        binding.imageBtn.setOnClickListener { handleImageBtnClick() }
        binding.btnViewList.setOnClickListener { handleViewListClick() }
    }

    // Handle the click on the save place button.
    private fun handleSavePlaceClick() {
        val text = binding.placeName.text.toString()
        val location = binding.textView.text.toString()
        val imageUri = imageUri?.toString() ?: ""
        viewModel.updateLocation(location)
        viewModel.addUserInput(text)
        viewModel.addEntry(imageUri)
        updateUIWithLatestEntry()
        binding.placeName.text.clear()
    }

    // Handle the click on the image button.
    private fun handleImageBtnClick() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    // Handle the click on the view list button.
    private fun handleViewListClick() {
        findNavController().navigate(R.id.action_addLocationFragment_to_allLocationsFragments)
    }

    // Handle the image pick.
    private fun handleImagePick(uri: Uri) {
        binding.resultImg.setImageURI(uri)
        requireActivity().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        imageUri = uri
    }

    // Handle the location permission result.
    private fun handleLocationPermissionResult(isGranted: Boolean) {
        if (isGranted) getLocationUpdates()
    }

    // Check and request location permission.
    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationUpdates()
        } else {
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Update the UI with the latest entry.
    private fun updateUIWithLatestEntry() {
        val text = binding.placeName.text.toString()
        val location = binding.textView.text.toString()
        val azimuth = viewModel.azimuth.value.toString()
        binding.nameView.text = text
        binding.coordinatesView.text = location
        binding.azimuthView.text = azimuth
        //NO NEED TO UPDATE IMAGE
    }

    // Get location updates.
    private fun getLocationUpdates() {
        viewModel.address.observe(viewLifecycleOwner) { binding.textView.text = it }
    }

    // Start listening for azimuth updates.
    override fun onResume() {
        super.onResume()
        azimuthSensorManager.startListening()
    }

    // Stop listening for azimuth updates.
    override fun onPause() {
        super.onPause()
        azimuthSensorManager.stopListening()
    }

    // Destroy the view.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
