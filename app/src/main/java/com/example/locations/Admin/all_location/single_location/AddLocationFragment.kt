package com.example.locations.Admin.all_location.single_location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.locations.Admin.all_location.AdminViewModel
import com.example.locations.R
import com.example.locations.databinding.AddLocationBinding

// AddLocationFragment is a Fragment that allows the user to add a new location entry.
class AddLocationFragment: Fragment() {
    // View binding
    private var _binding: AddLocationBinding? = null
    private val binding get() = _binding!!
    // Uri of the image selected by the user.
    private var imageUri: Uri? = null

    // Instance of AdminViewModel to access the data.
    private val viewModel: AdminViewModel by activityViewModels()

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
        binding.resultImg.setOnClickListener { handleImageBtnClick() }
        binding.btnViewList.setOnClickListener { handleViewListClick() }
        binding.btnBack.setOnClickListener{ handleBackClick()}
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackClick()
            }

        })
    }
    private fun handleBackClick() {
        findNavController().navigate(R.id.action_addLocationFragment_to_Nav)
    }

    // Handle the click on the save place button.
    private fun handleSavePlaceClick() {
        val textLayout = binding.placeNameEditText
        val text = textLayout.editText?.text.toString()
        val location = binding.coordText.text.toString()
        val imageUri = imageUri?.toString() ?: ""
        if(text.isEmpty()){
            toggleNameError(true)
        }else{
            toggleNameError(false)
            viewModel.updateLocation(location)
            viewModel.addUserInput(text)
            viewModel.addEntry(imageUri)
            updateUIWithLatestEntry()
        }
    }

    private fun toggleNameError(isError:Boolean){
        val textLayout = binding.placeNameEditText
       if(isError){
           textLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
           textLayout.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.red)
           textLayout.setStartIconTintList(
               ColorStateList.valueOf(
                   ContextCompat.getColor(
                       requireContext(), R.color.red
                   )
               )
           )
           val shakeAnimation= AnimationUtils.loadAnimation(context, R.anim.vibrate)
           textLayout.startAnimation(shakeAnimation)
       }else{
           textLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.btnsAndInput)
           textLayout.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.btnsAndInput)
           textLayout.setStartIconTintList(
               ColorStateList.valueOf(
                   ContextCompat.getColor(
                       requireContext(), R.color.btnsAndInput
                   )
               )
           )
       }
    }
    // Handle the click on the image button.
    private fun handleImageBtnClick() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    // Handle the click on the view list button.
    private fun handleViewListClick() {
        val bundle= bundleOf("returnToFragmentId" to R.id.action_allLocationsFragments_to_addLocationFragment)
        findNavController().navigate(R.id.action_addLocationFragment_to_allLocationsFragments,bundle)
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
        val text = binding.placeNameEditText.editText?.text.toString()
        val location = binding.coordText.text.toString()
        val azimuth = viewModel.azimuth.value.toString()
        binding.nameView.text = text
        binding.coordinatesView.text = location
        binding.azimuthView.text = azimuth
        binding.placeNameEditText.editText?.setText("")
        //NO NEED TO UPDATE IMAGE
    }

    // Get location updates.
    private fun getLocationUpdates() {
        viewModel.address.observe(viewLifecycleOwner) {
            val data = it.split(",")
            val latitude = data[0]
            val longitude = data[1]
            val accuracy = data[2].toFloat()
            binding.coordText.text = "${latitude} , ${longitude}"
            binding.accuracy.text = "Accuracy: ${accuracy} meters"
        }
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

