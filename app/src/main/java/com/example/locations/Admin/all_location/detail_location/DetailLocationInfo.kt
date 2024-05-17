package com.example.locations.Admin.all_location.detail_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.locations.Admin.all_location.AdminViewModel
import com.example.locations.R
import com.example.locations.databinding.DetailLocationInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Fragment class for the detail view of a location
class DetailLocationInfo : Fragment() {
    // Binding object instance corresponding to the detail_location_info.xml layout
    private var _binding : DetailLocationInfoBinding? = null
    private val binding get() = _binding!!

    // Get the view model
    private val viewModel: AdminViewModel by activityViewModels()

    // Inflate the layout defined in detail_location_info.xml and return the root view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailLocationInfoBinding.inflate(inflater , container , false)
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_detailLocationInfo_to_allLocationsFragments)
        }

        return binding.root
    }

    // Set the text and image of the chosen item in the detail view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.chosenItem.observe(viewLifecycleOwner){
            val auth= Firebase.auth
            val currentUser=auth.currentUser?.email.toString()
            val admin="navigationproject2024@gmail.com"
            val coords=   binding.locationLocation
            val azimuth= binding.locationAzimuth
            binding.locationName.text = it.text
            coords.text = it.location
            azimuth.text = it.azimuth
            if(currentUser==admin) {
                coords.visibility=View.VISIBLE
                azimuth.visibility=View.VISIBLE
            }else{
                binding.description.visibility=View.VISIBLE
            }

            Glide.with(binding.root).load(it.img).into(binding.itemDetailImage)
        }
    }

    // Set the binding object to null when the fragment view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}