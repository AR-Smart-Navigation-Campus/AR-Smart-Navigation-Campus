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

class AddLocationFragment: Fragment() {
    private var _binding: AddLocationBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null


    private val viewModel: MainViewModel by activityViewModels()

    // Instance of AzimuthSensorManager to get azimuth updates.
    private lateinit var azimuthSensorManager: AzimuthSensorManager

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            handleImagePick(it)
        }
    }
    private val locationRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { handleLocationPermissionResult(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AddLocationBinding.inflate(inflater, container, false)
        // Initialize AzimuthSensorManager and set azimuth update listener.
        azimuthSensorManager = AzimuthSensorManager(requireContext()) { azimuth ->
            viewModel.updateAzimuth(azimuth)
        }
        setupView()
        return binding.root
    }

    private fun setupView() {
        checkAndRequestLocationPermission()
        binding.btnSavePlace.setOnClickListener { handleSavePlaceClick() }
        binding.imageBtn.setOnClickListener { handleImageBtnClick() }
        binding.btnViewList.setOnClickListener { handleViewListClick() }
    }

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

    private fun handleImageBtnClick() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    private fun handleViewListClick() {
        findNavController().navigate(R.id.action_addLocationFragment_to_allLocationsFragments)
    }

    private fun handleImagePick(uri: Uri) {
        binding.resultImg.setImageURI(uri)
        requireActivity().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        imageUri = uri
    }

    private fun handleLocationPermissionResult(isGranted: Boolean) {
        if (isGranted) getLocationUpdates()
    }

    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationUpdates()
        } else {
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun updateUIWithLatestEntry() {
        val text = binding.placeName.text.toString()
        val location = binding.textView.text.toString()
        val azimuth = viewModel.azimuth.value.toString()
        binding.nameView.text = text
        binding.coordinatesView.text = location
        binding.azimuthView.text = azimuth
        //NO NEED TO UPDATE IMAGE
    }

    private fun getLocationUpdates() {
        viewModel.address.observe(viewLifecycleOwner) { binding.textView.text = it }
    }

    override fun onResume() {
        super.onResume()
        azimuthSensorManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        azimuthSensorManager.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
