package com.example.arnavigationapp.admin.all_location.detail_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.arnavigationapp.admin.all_location.AdminViewModel
import com.example.arnavigationapp.R
import com.example.arnavigationapp.databinding.DetailLocationInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Fragment class for the detail view of a location
class DetailLocationInfo : Fragment() {

    // Binding object instance corresponding to the detail_location_info.xml layout
    private var _binding: DetailLocationInfoBinding? = null
    private val binding get() = _binding!!

    // Get the view model
    private val viewModel: AdminViewModel by activityViewModels()

    // Inflate the layout defined in detail_location_info.xml and return the root view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailLocationInfoBinding.inflate(inflater, container, false)
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_detailLocationInfo_to_allLocationsFragments)
        }

        return binding.root
    }

    // Set the text and image of the chosen item in the detail view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe the chosen item in the view model
        viewModel.chosenItem.observe(viewLifecycleOwner) { it ->
            val auth = Firebase.auth // Firebase authentication instance
            val currentUser = auth.currentUser?.email.toString() // Get the current user's email
            val resId =
                viewModel.getLocationNameResId(it.name) // Get the resource ID of the location name
            if (resId == R.string.unknown_location) {
                binding.locationName.text = it.name
            } else {
                binding.locationName.text =
                    binding.root.context.getString(resId) // Set the location name text view
            }
            val coordsText =
                getString(R.string.coordinates) + ": " + it.location.replace("\\s+".toRegex(), "")
            binding.locationLocation.text = coordsText
            val azimuthText = getString(R.string.azimuth) + ": " + it.azimuth
            binding.locationAzimuth.text = azimuthText
            val descriptionId = viewModel.getLocationDescriptionResId(it.description)
            var descriptionText = getString(R.string.description) + ": "
            if (descriptionId == R.string.no_desc) {
                descriptionText+= it.description
            } else {
                descriptionText += binding.root.context.getString(descriptionId)
            }
            binding.description.text = descriptionText
            binding.description.visibility = View.VISIBLE
            if (currentUser != viewModel.admin) {
                binding.locationLocation.visibility = View.GONE
                binding.locationAzimuth.visibility = View.GONE
            }
            Glide.with(binding.root).load(it.imgUrl).circleCrop()
                .into(binding.itemDetailImage) // Load the image into the image view
        }
    }

    // Set the binding object to null when the fragment view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}