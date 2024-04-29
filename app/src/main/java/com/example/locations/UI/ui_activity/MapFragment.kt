package com.example.locations.UI.ui_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.locations.databinding.MapFragmentBinding
import com.example.locations.R

class MapFragment : Fragment() {
    private lateinit var binding: MapFragmentBinding
    private var ar = R.id.action_MapFragment_to_AR
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MapFragmentBinding.inflate(inflater, container, false)

        binding.buttonbuilding1.setOnClickListener {
            findNavController().navigate(ar)
        }

        binding.buttonbuilding2.setOnClickListener {
            findNavController().navigate(ar)
        }

        binding.buttonbuilding3.setOnClickListener {
            findNavController().navigate(ar)
        }

        binding.buttonbuilding4.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttonbuilding5.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttonbuilding6.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttonbuilding7.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttonbuilding8.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttonaguda.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttoncafeteria.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttonclub.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttonlibrary.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.buttonstore.setOnClickListener {
            findNavController().navigate(ar)        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_Map_to_StartNav)
        }

        return binding.root
    }
}