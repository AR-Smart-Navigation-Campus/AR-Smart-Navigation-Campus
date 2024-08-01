package com.example.arnavigationapp.admin.all_location.single_location

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.arnavigationapp.admin.all_location.AdminViewModel
import com.example.arnavigationapp.R
import com.example.arnavigationapp.databinding.AddLocationBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AddLocationFragment is a Fragment that allows the user to add a new location entry
 * to the database. The admin can enter the name of the location, select an image, and
 * save the location. The admin can also view the list of all locations and go back to the
 * home screen.
 * The admin can also view the current location and azimuth.
 */
class AddLocationFragment: Fragment() {

    // View binding
    private var _binding: AddLocationBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null // Uri of the image selected by the user
    private lateinit var currentPhotoPath: String // Path of the current photo


    private val viewModel: AdminViewModel by activityViewModels() // Instance of AdminViewModel to access the data

    private lateinit var azimuthSensorManager: AzimuthSensorManager // Instance of AzimuthSensorManager to get azimuth updates.


    // Register a launcher for taking a picture.
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                handleImagePick(it)
            }
        } else {
            Toast.makeText(context, getString(R.string.could_not_take_picture), Toast.LENGTH_SHORT).show()
        }
    }

    // Register a launcher for picking an image.
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            handleImagePick(it)
        }
    }

    // Create the view.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AddLocationBinding.inflate(inflater, container, false)

        // Initialize AzimuthSensorManager and set azimuth update listener
        azimuthSensorManager = AzimuthSensorManager(requireContext()) { azimuth ->
            viewModel.updateAzimuth(azimuth)
        }
        setupView()

        return binding.root
    }

    // Set up the view.
    private fun setupView() {
        getLocationUpdates()
        binding.btnSavePlace.setOnClickListener { handleSavePlaceClick() }
        binding.resultImg.setOnClickListener { showPopupMenu(it) }
        binding.btnViewList.setOnClickListener { handleViewListClick() }
        binding.btnAddPlaceToMap.setOnClickListener { handleAddToMapClick() }
        binding.btnBack.setOnClickListener{ handleBackClick()}
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackClick()
            }

        })
    }



    private fun handleTakePictureClick() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", it)
            imageUri = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    // Handle the click on the image button.
    private fun handleImageBtnClick() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }


    // Handle the image pick.
    private fun handleImagePick(uri: Uri) {
        binding.resultImg.setImageURI(uri)
        imageUri = uri
    }



    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.pick_img)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_choose_from_gallery -> {
                    // Handle choose from gallery
                    handleImageBtnClick()
                    true
                }
                R.id.action_take_photo -> {
                    // Handle take photo
                    handleTakePictureClick()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }


    // Handle the click on the back button.
    private fun handleBackClick() {
        findNavController().navigate(R.id.action_addLocationFragment_to_Nav)
    }

    // Handle the click on the save place button.
    private fun handleSavePlaceClick() {
        val textLayout = binding.placeNameEditText
        val locationName = textLayout.editText?.text.toString()
        val descriptionLayout = binding.descriptionEditText
        val description = descriptionLayout.editText?.text.toString()
        val location = binding.coordText.text.toString()
        if(locationName.isEmpty()){
            toggleNameError(true)
        }else{
            toggleNameError(false)
            viewModel.updateLocation(location)
            viewModel.addUserInput(locationName)
            viewModel.addDescriptionInput(description)

            // Check if imageUri is not null
            imageUri?.let { uri ->
                viewModel.addEntry(uri)
                updateUIWithLatestEntry()
            } ?: run {
                Toast.makeText(context, getString(R.string.select_image), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle the click on the view list button.
    private fun handleViewListClick() {
        val bundle= bundleOf("returnToFragmentId" to R.id.action_allLocationsFragments_to_addLocationFragment)
        findNavController().navigate(R.id.action_addLocationFragment_to_allLocationsFragments,bundle)
    }

    // Handle the click on the add to map button.
    private fun handleAddToMapClick(){
        val textLayout = binding.placeNameEditText
        val locationName = textLayout.editText?.text.toString()
        if(locationName.isEmpty()){
            toggleNameError(true)
        }else {
            toggleNameError(false)
            viewModel.addUserInput(locationName)
            val bundle =
                bundleOf("returnToFragmentId" to R.id.action_MapFragment_to_addLocationFragment)
            findNavController().navigate(R.id.action_addLocationFragment_to_mapFragment, bundle)
        }
    }

    // Update the UI with the latest entry.
    private fun updateUIWithLatestEntry() {
        val name = binding.placeNameEditText.editText?.text.toString()
        val location = binding.coordText.text.toString()
        val azimuth = viewModel.azimuth.value.toString()

        val nameText = getString(R.string.name) + ": " + name
        binding.nameView.text = nameText
        val coordsText = getString(R.string.coordinates) + ": " + location
        binding.coordinatesView.text = coordsText
        val azimuthText = getString(R.string.azimuth) + ": " + azimuth
        binding.azimuthView.text = azimuthText

        binding.placeNameEditText.editText?.text?.clear()
        binding.descriptionEditText.editText?.text?.clear()
        //NO NEED TO UPDATE IMAGE
    }

    // Toggle the error state of the name text layout.
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
                    ContextCompat.getColor(requireContext(), R.color.btnsAndInput)))
        }
    }

    // Get location updates.
    private fun getLocationUpdates() {
        viewModel.address.observe(viewLifecycleOwner) { currLocation ->
            val data = currLocation.split(",")
            val latitude = data[0]
            val longitude = data[1]
            val accuracy = data[2].toFloat()
            "${latitude},${longitude}".also { binding.coordText.text = it } // Update the text view with the location

            val accuracyText = getString(R.string.accuracy) + ": " + accuracy + " "+ getString(R.string.meters_accuracy)
            binding.accuracy.text = accuracyText
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

