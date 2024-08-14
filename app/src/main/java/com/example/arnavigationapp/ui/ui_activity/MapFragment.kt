package com.example.arnavigationapp.ui.ui_activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.arnavigationapp.admin.all_location.AdminViewModel
import com.example.arnavigationapp.admin.all_location.model.LocationData
import com.example.arnavigationapp.databinding.MapFragmentBinding
import com.example.arnavigationapp.R
import com.example.arnavigationapp.admin.all_location.model.FirestoreRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

/**
 *  MapFragment class that extends Fragment and provides functionality for the map screen.
 */

class MapFragment : Fragment() {
    private lateinit var binding: MapFragmentBinding // View binding for the fragment's layout
    private val viewModel: AdminViewModel by activityViewModels() // Shared ViewModel between fragments
    private val repository = FirestoreRepository() // FirestoreRepository object for data operations
    private var fragmentId: Int? = null // Fragment ID for navigation
    private val ar = R.id.action_MapFragment_to_AR // Action ID to navigate to AR fragment
    private val loadedButtons =
        mutableSetOf<String>() // Set to track loaded buttons to prevent duplication

    // Define map's GPS boundaries
    companion object {
        const val MIN_LATITUDE = 32.0139
        const val MAX_LATITUDE = 32.015713
        const val MIN_LONGITUDE = 34.7727
        const val MAX_LONGITUDE = 34.7743
    }

    // Inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentId = arguments?.getInt("returnToFragmentId") // Get the fragment ID from arguments
        binding = MapFragmentBinding.inflate(
            inflater,
            container,
            false
        ) // Inflate the layout using View Binding

        val flickerAnimation =
            AnimationUtils.loadAnimation(context, R.anim.flicker) // Load animation
        if (R.id.action_MapFragment_to_addLocationFragment != fragmentId) {
            // Setup for normal map view
            binding.outOfBounds.startAnimation(flickerAnimation)
            binding.pickPlace.startAnimation(flickerAnimation)
            binding.addPlaceOnMap.visibility = View.GONE
            convertLatLngToScreenPosition()
            setupButtonListeners()
        } else {
            // Setup for adding locations
            binding.addPlaceOnMap.startAnimation(flickerAnimation)
            binding.outOfBounds.visibility = View.GONE
            binding.pickPlace.visibility = View.GONE
            setupDynamicButtonCreation()
        }

        if (loadedButtons.isEmpty()) {
            loadDynamicButtons() // Load dynamic buttons from Firestore
        }

        // Handle back press
        setupBackPressHandler()

        // Setup back button click listener
        binding.btnBack.setOnClickListener {
            // Hide the location icon and reset its position
            binding.locationIcon.apply {
                visibility = View.GONE
                x = 0f
                y = 0f
            }
            returnToAdd() // Navigate back to add location fragment
        }

        return binding.root // Return the root view of the fragment
    }



    override fun onResume() {
        super.onResume()
        clearDynamicButtons() // Clear buttons to prevent duplication
        loadDynamicButtons() // Reload buttons from Firestore
    }

    // Load buttons state from Firestore
    private fun loadDynamicButtons() {
        repository.loadButtonStateFromFirestore { buttons ->
            context?.let {
                buttons.forEach { buttonData ->
                    if (buttonData.text.isNotEmpty() && !loadedButtons.contains(buttonData.text)) {
                        createButtonAtPosition(buttonData.x, buttonData.y, buttonData.text)
                        loadedButtons.add(buttonData.text) // Track created buttons
                    }
                }
            }
        }
    }

    // Clear dynamic buttons to prevent duplication
    private fun clearDynamicButtons() {
        val parentLayout = binding.root as ViewGroup // Get the parent layout
        for (i in parentLayout.childCount - 1 downTo 0) {
            val view = parentLayout.getChildAt(i)
            if (view is Button && view.id == View.NO_ID) {
                parentLayout.removeView(view) // Remove dynamic buttons
            }
        }
        loadedButtons.clear() // Clear the loaded buttons set
    }

    // Setup dynamic button creation
    @SuppressLint("ClickableViewAccessibility")
    private fun setupDynamicButtonCreation() {
        binding.mapImageView.setOnTouchListener { v, event ->
            val btnName =
                viewModel.userInput.value // Get the name for the new button from ViewModel
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.performClick()
                if (!btnName.isNullOrEmpty() && !loadedButtons.contains(btnName)) {
                    createButtonAtPosition(
                        event.x,
                        event.y,
                        btnName
                    ) // Create a new button at the touched position
                    repository.saveButtonStateToFirestore(
                        event.x,
                        event.y,
                        btnName
                    ) // Save the button state to Firestore
                    loadedButtons.add(btnName) // Add the button to the loaded set
                    Toast.makeText(context, getString(R.string.add_location_msg), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Button with this name already exists or name is empty.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            } else false
        }
    }

    // Create a button at the specified position
    @SuppressLint("ClickableViewAccessibility")
    private fun createButtonAtPosition(x: Float, y: Float, btnName: String) {
        var translatedText = ""
        val stringResId =
            viewModel.getLocationNameResId(btnName) // Get resource ID for the location name
        if (stringResId == R.string.unknown_location) {
            translatedText = btnName // If no resource ID, use the button name directly
        } else {
            translatedText =
                binding.root.context.getString(stringResId) // Get the string from resource ID
        }
        // Check if the button is already created
        if (loadedButtons.contains(translatedText)) return

        val button = Button(context).apply {
            // Set button text from the ViewModel's userInput
            text = translatedText

            setPadding(0, 0, 0, 0) // Remove default padding
            textSize = 10f // Set text size
            minWidth = 0 // Remove minimum width
            minHeight = 0 // Remove minimum height

            // Set background to null to remove default styling
            background = null

            // Set button position and size
            layoutParams = ConstraintLayout.LayoutParams(
                200, 50 // Fixed width and height for the button
            ).apply {
                topToTop = binding.mapImageView.id
                leftToLeft = binding.mapImageView.id
                marginStart = x.toInt()
                topMargin = y.toInt()
            }
            // Set the button's appearance
            setBackgroundColor(Color.parseColor("#80FFFFFF")) // White with 50% opacity
            setTextColor(Color.BLACK) // Set text color to black
        }

        if (R.id.action_MapFragment_to_addLocationFragment == fragmentId) {
            // Make the button draggable and update its position in Firestore
            button.setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Store the initial position of the touch
                        view.tag = Pair(motionEvent.rawX, motionEvent.rawY)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // Calculate the distance moved
                        val (initialRawX, initialRawY) = view.tag as Pair<Float, Float>
                        val deltaX = motionEvent.rawX - initialRawX
                        val deltaY = motionEvent.rawY - initialRawY

                        // Update the position of the button
                        view.x += deltaX
                        view.y += deltaY

                        // Store the new position
                        view.tag = Pair(motionEvent.rawX, motionEvent.rawY)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        // Save the final position to Firestore when the drag ends
                        val newX = view.x
                        val newY = view.y
                        repository.updateButtonPositionInFirestore(translatedText, newX, newY)
                        true
                    }

                    else -> false
                }
            }
        } else {
            // Setup click listener for buttons when not adding new locations
            button.setOnClickListener {
                findLocationByName(btnName)
            }
        }
        binding.root.addView(button) // Add the button to the layout
    }

    // Return to add location fragment
    private fun returnToAdd() {
        val returnToFragmentId =
            arguments?.getInt("returnToFragmentId") // Get the return fragment ID from arguments
        if (returnToFragmentId != null) {
            Log.d("returnToFragmentId", returnToFragmentId.toString())
            findNavController().navigate(returnToFragmentId) // Navigate to the specified fragment
        } else {
            findNavController().navigate(R.id.action_Map_to_Nav) // Navigate to the default fragment
        }
    }

    // Setup back press handling to return to the add location fragment
    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    returnToAdd() // Handle the back press
                }
            })
    }

    // Get the location by name
    private fun getLocationByName(
        locationName: String,
        allLocations: List<LocationData>
    ): LocationData? {

        return allLocations.find {
            it.name.equals(locationName, ignoreCase = true)
        }
    }

    // Find the location by name and navigate to AR fragment if found
    private fun findLocationByName(locationName: String) {
        viewModel.locationData.observe(viewLifecycleOwner) { allLocations ->
            if (allLocations.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_locations_available),
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            val location = getLocationByName(locationName, allLocations) // Get the location by name
            if (location != null) {
                viewModel.setLocation(location) // Set the selected location
                findNavController().navigate(ar) // Navigate to the AR fragment
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_location_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Placeholder function to convert GPS coordinates to screen position
    private fun convertLatLngToScreenPosition() {
        viewModel.address.observe(viewLifecycleOwner) { address ->
            val myLocation = viewModel.createLocation(address)
            val mapWidth = binding.root.width
            val mapHeight = binding.root.height

            val x_coord =
                (mapWidth * ((myLocation.longitude - MIN_LONGITUDE) / (MAX_LONGITUDE - MIN_LONGITUDE))).toFloat() // Calculate the x coordinate
            val y_coord =
                (mapHeight * (1 - (myLocation.latitude - MIN_LATITUDE) / (MAX_LATITUDE - MIN_LATITUDE))).toFloat() // Calculate the y coordinate

            // Check if the location is out of bounds
            if (myLocation.latitude < MIN_LATITUDE || myLocation.latitude > MAX_LATITUDE || myLocation.longitude < MIN_LONGITUDE || myLocation.longitude > MAX_LONGITUDE) {
                binding.outOfBounds.visibility = View.VISIBLE
            } else {
                binding.outOfBounds.visibility = View.GONE
            }

            binding.locationIcon.apply {
                visibility = View.VISIBLE
                x = x_coord
                y = y_coord
            }
        }
    }

    // Set up button listeners for predefined buttons
    private fun setupButtonListeners() {
        val buttonLocationMap = mapOf(
            binding.buttonFichmanGate to "Fichman Gate",
            binding.buttonHoffinGate to "Hoffin Gate",
            binding.buttonGolombGate to "Golomb Gate",
            binding.buttonbuilding1 to "Building 1",
            binding.buttonbuilding2 to "Building 2",
            binding.buttonbuilding3A to "Building 3-A",
            binding.buttonbuilding3B to "Building 3-B",
            binding.buttonbuilding4 to "Building 4-Workshop",
            binding.buttonbuilding5 to "Building 5",
            binding.buttonbuilding6 to "Building 6",
            binding.buttonbuilding7 to "Building 7",
            binding.buttonbuilding8A to "Building 8-A",
            binding.buttonbuilding8B to "Building 8-B",
            binding.buttonbuilding8C to "Building 8-C",
            binding.buttonaguda to "Aguda",
            binding.buttoncafeteria to "Cafeteria",
            binding.buttonclub to "Club",
            binding.buttonlibrary to "Library",
            binding.buttonstore to "Materials Shop"
        )
        buttonLocationMap.forEach { (button, location) ->
            button.setOnClickListener {
                findLocationByName(location) // Setup listener for each predefined button
            }
        }
    }

    // Destroy view binding when fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        binding.locationIcon.apply {
            visibility = View.GONE
            x = 0f
            y = 0f
        }
    }
}
