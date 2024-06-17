package com.example.locations.UI.ui_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.locations.Admin.all_location.AdminViewModel
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.databinding.MapFragmentBinding
import com.example.locations.R

class MapFragment : Fragment() {
    private lateinit var binding: MapFragmentBinding
    private val viewModel: AdminViewModel by activityViewModels()
    private val ar = R.id.action_MapFragment_to_AR


    private fun getLocationByName(locationName: String, allLocations: List<LocationData>): LocationData? {
        return allLocations.find {
            it.name.equals(locationName, ignoreCase = true)
        }
    }


    private fun findLocationByName(locationName: String) {
        viewModel.locationData.observe(viewLifecycleOwner) { allLocations ->
            if (allLocations.isEmpty()) {
                Toast.makeText(requireContext(), "Location data is not available", Toast.LENGTH_SHORT).show()
                return@observe
            }
            val location = getLocationByName(locationName, allLocations)
            if (location != null) {
                viewModel.setLocation(location)
                findNavController().navigate(ar)
                Toast.makeText(requireContext(), "Location found: ${location.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MapFragmentBinding.inflate(inflater, container, false)

        binding.buttonFichmanGate.setOnClickListener{
            findLocationByName("Fichman Gate")
        }

        binding.buttonHoffinGate.setOnClickListener{
            findLocationByName("Hoffin Gate")
        }

        binding.buttonGolombGate.setOnClickListener{
            findLocationByName("Golomb Gate")
        }


        binding.buttonbuilding1.setOnClickListener {
            findLocationByName("Building 1")
        }

        binding.buttonbuilding2.setOnClickListener {
            findLocationByName("Building 2")
        }

        binding.buttonbuilding3A.setOnClickListener {
            findLocationByName("Building 3 A")
        }

        binding.buttonbuilding3B.setOnClickListener {
            findLocationByName("Building 3 B")
        }

        binding.buttonbuilding4.setOnClickListener {
            findLocationByName("Building 4")
        }

        binding.buttonbuilding5.setOnClickListener {
            findLocationByName("Building 5")
        }

        binding.buttonbuilding6.setOnClickListener {
            findLocationByName("Building 6")
        }

        binding.buttonbuilding7.setOnClickListener {
            findLocationByName("Building 7")
        }

        binding.buttonbuilding8A.setOnClickListener {
            findLocationByName("Building 8 A")
        }

        binding.buttonbuilding8B.setOnClickListener {
            findLocationByName("Building 8 B")
        }

        binding.buttonbuilding8C.setOnClickListener {
            findLocationByName("Building 8 C")
        }

        binding.buttonaguda.setOnClickListener {
            findLocationByName("Aguda")
        }

        binding.buttoncafeteria.setOnClickListener {
            findLocationByName("Cafeteria")
        }

        binding.buttonclub.setOnClickListener {
            findLocationByName("Club")
        }


        binding.buttonlibrary.setOnClickListener {
            findLocationByName("library")
        }

        binding.buttonstore.setOnClickListener {
            findLocationByName("Store")
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_Map_to_StartNav)
        }

        return binding.root
    }
}