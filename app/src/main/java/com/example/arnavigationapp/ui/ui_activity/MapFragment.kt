package com.example.arnavigationapp.ui.ui_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.arnavigationapp.admin.all_location.AdminViewModel
import com.example.arnavigationapp.admin.all_location.model.LocationData
import com.example.arnavigationapp.databinding.MapFragmentBinding
import com.example.arnavigationapp.R

/**
 *  MapFragment class that extends Fragment and provides functionality for the map screen.
 */

class MapFragment : Fragment() {
    private lateinit var binding: MapFragmentBinding
    private val viewModel: AdminViewModel by activityViewModels()
    private val ar = R.id.action_MapFragment_to_AR

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
        binding = MapFragmentBinding.inflate(inflater, container, false)

        convertLatLngToScreenPosition()

        setupButtonListeners()

        return binding.root
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
                    getString(R.string.location_found) +": " + location.name,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.no_location_found), Toast.LENGTH_SHORT).show()
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
            }
            else {
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
        binding.btnBack.setOnClickListener {
            // Hide the location icon
            binding.locationIcon.apply {
                visibility = View.GONE
                x = 0f
                y = 0f
            }
            findNavController().navigate(R.id.action_Map_to_StartNav)
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