package com.example.locations.UI.ui_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.locations.databinding.StartNavFragmentBinding
import com.example.locations.R

class StartNavFragment : Fragment() {

    private lateinit var binding : StartNavFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StartNavFragmentBinding.inflate(inflater, container, false)

        binding.btnList.setOnClickListener {
            findNavController().navigate(R.id.action_StartNav_to_allLocationsFragments)
        }

        binding.btnMap.setOnClickListener {
            findNavController().navigate(R.id.action_StartNav_to_Map)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_StartNav_to_Nav)
        }
        return binding.root
    }
}