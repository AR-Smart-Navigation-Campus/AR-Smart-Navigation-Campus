package com.example.locations.detail_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.locations.MainViewModel
import com.example.locations.R
import com.example.locations.databinding.DetailLocationInfoBinding

class DetailLocationInfo : Fragment() {
    private var _binding : DetailLocationInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.chosenItem.observe(viewLifecycleOwner){
            binding.locationName.text = it.text
            binding.locationLocation.text = it.location
            binding.locationAzimuth.text = it.azimuth
            Glide.with(binding.root).load(it.img).circleCrop().into(binding.itemDetailImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//package com.example.locations.detail_location
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.navigation.fragment.findNavController
//import com.bumptech.glide.Glide
//import com.example.locations.LocationsViewModel
//import com.example.locations.R
//import com.example.locations.data.model.LocationData
//import com.example.locations.databinding.DetailLocationInfoBinding
//
//class DetailLocationInfo : Fragment() {
//    private var _binding : DetailLocationInfoBinding? = null
//    private val binding get() = _binding!!
//    val viewModel : LocationsViewModel by activityViewModels()
//
//    fun bindDetail(location: LocationData){
//        binding.locationName.text = location.text
//        binding.locationLocation.text = location.location
//        binding.locationAzimuth.text = location.azimuth
//        Glide.with(binding.root).load(location.img).circleCrop().into(binding.itemDetailImage)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = DetailLocationInfoBinding.inflate(inflater , container , false)
//        binding.btnBack.setOnClickListener {
//            findNavController().navigate(R.id.action_detailLocationInfo_to_allLocationsFragments)
//        }
//
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewModel.chosenItem.observe(viewLifecycleOwner){
//            binding.locationName.text = it.text
//            binding.locationLocation.text = it.location
//            binding.locationAzimuth.text = it.azimuth
//            Glide.with(binding.root).load(it.img).circleCrop().into(binding.itemDetailImage)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//}