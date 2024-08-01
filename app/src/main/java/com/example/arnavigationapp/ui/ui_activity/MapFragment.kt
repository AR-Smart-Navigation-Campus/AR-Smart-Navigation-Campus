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

/**
 *  MapFragment class that extends Fragment and provides functionality for the map screen.
 *
 */

class MapFragment : Fragment() {
    private lateinit var binding: MapFragmentBinding
    private val viewModel: AdminViewModel by activityViewModels()
    private val repository = FirestoreRepository() // FirestoreRepository object
    private var fragmentId: Int? = null
    private val addLocationId = 2131230786
    private val ar = R.id.action_MapFragment_to_AR
    private val loadedButtons = mutableSetOf<String>() // Set to track loaded buttons


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
        fragmentId = arguments?.getInt("returnToFragmentId")
        binding = MapFragmentBinding.inflate(inflater, container, false)

        val flickerAnimation = AnimationUtils.loadAnimation(context, R.anim.flicker)
        if (fragmentId != addLocationId) {
            binding.outOfBounds.startAnimation(flickerAnimation)
            binding.pickPlace.startAnimation(flickerAnimation)
            binding.addPlaceOnMap.visibility = View.GONE
            convertLatLngToScreenPosition()
            setupButtonListeners()
        } else {
            binding.addPlaceOnMap.startAnimation(flickerAnimation)
            binding.outOfBounds.visibility = View.GONE
            binding.pickPlace.visibility = View.GONE
            setupDynamicButtonCreation()
        }

        if (loadedButtons.isEmpty()) {
            loadDynamicButtons()
        }

        // Handle back press
        setupBackPressHandler()

        binding.btnBack.setOnClickListener {
            // Hide the location icon
            binding.locationIcon.apply {
                visibility = View.GONE
                x = 0f
                y = 0f
            }
            returnToAdd()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        clearDynamicButtons()
        loadDynamicButtons()
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
        val parentLayout = binding.root as ViewGroup
        for (i in parentLayout.childCount - 1 downTo 0) {
            val view = parentLayout.getChildAt(i)
            if (view is Button && view.id == View.NO_ID) {
                parentLayout.removeView(view)
            }
        }
        loadedButtons.clear()
    }

    // Setup dynamic button creation
    @SuppressLint("ClickableViewAccessibility")
    private fun setupDynamicButtonCreation() {
        binding.mapImageView.setOnTouchListener { v, event ->
            val btnName = viewModel.userInput.value
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.performClick()
                if (!btnName.isNullOrEmpty() && !loadedButtons.contains(btnName)) {
                    createButtonAtPosition(event.x, event.y, btnName)
                    repository.saveButtonStateToFirestore(event.x, event.y, btnName)
                    loadedButtons.add(btnName)
                } else {
                    Toast.makeText(context, "Button with this name already exists or name is empty.", Toast.LENGTH_SHORT).show()
                }
                true
            } else false
        }
    }


    // Create a button at the specified position
    @SuppressLint("ClickableViewAccessibility")
    private fun createButtonAtPosition(x: Float, y: Float, btnName: String) {
        var translatedText = ""
        val stringResId = viewModel.getLocationNameResId(btnName)
        if(stringResId == R.string.unknown_location){
            translatedText = btnName
        }
        else{
            translatedText = binding.root.context.getString(stringResId)
        }

        // Check if the button is already created
        if (loadedButtons.contains(translatedText)) return

        val button = Button(context).apply {
            // Set button text from the viewModel's userInput
            text = translatedText

            setPadding(0, 0, 0, 0)// Remove default padding
            textSize = 10f // Set text size
            minWidth = 0 // Remove minimum width
            minHeight = 0 // Remove minimum height

            // Set background to null to remove default styling
            background = null

            // Set button position and size
            layoutParams = ConstraintLayout.LayoutParams(
                //ConstraintLayout.LayoutParams.WRAP_CONTENT,
                //ConstraintLayout.LayoutParams.WRAP_CONTENT,
                200,50

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

        if(fragmentId==addLocationId) {
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
        } else{
            button.setOnClickListener {
                findLocationByName(translatedText)
            }
        }
        binding.root.addView(button)
    }

    // Return to add location fragment
    private fun returnToAdd() {
        val returnToFragmentId = arguments?.getInt("returnToFragmentId")
        if (returnToFragmentId != null) {
            Log.d("returnToFragmentId", returnToFragmentId.toString())
            findNavController().navigate(returnToFragmentId)
        } else {
            findNavController().navigate(R.id.action_Map_to_Nav)
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    returnToAdd()
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


    // Find the location by name
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
                findNavController().navigate(ar)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_found) + ": " + location.name,
                    Toast.LENGTH_SHORT
                ).show()
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


    // Set up button listeners
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
                findLocationByName(location)
            }
        }
    }

    // Destroy view binding
    override fun onDestroyView() {
        super.onDestroyView()
        binding.locationIcon.apply {
            visibility = View.GONE
            x = 0f
            y = 0f
        }
    }
}