package com.example.locations.ui.ui_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.locations.databinding.StartNavFragmentBinding
import com.example.locations.R

/**
 * A fragment representing the start navigation screen.
 */

class StartNavFragment : Fragment() {

    private lateinit var binding : StartNavFragmentBinding

    // Inflate the layout for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StartNavFragmentBinding.inflate(inflater, container, false)

        binding.btnList.setOnClickListener {
            val bundle = bundleOf("returnToFragmentId" to R.id.action_allLocationsFragments_to_StartNav )
            findNavController().navigate(R.id.action_StartNav_to_allLocationsFragments,bundle)
        }

        binding.btnMap.setOnClickListener {
            findNavController().navigate(R.id.action_StartNav_to_Map)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_StartNav_to_Nav)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_StartNav_to_Nav)
            }

        })
        return binding.root
    }
}