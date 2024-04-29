package com.example.locations.UI.ui_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.NavigationFragmentBinding

class NavigationFragment : Fragment() {
    private lateinit var binding : NavigationFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = NavigationFragmentBinding.inflate(inflater, container, false)
        binding.btnStartNav.setOnClickListener {
            findNavController().navigate(R.id.action_Nav_to_StartNav)
        }

        binding.btnNews.setOnClickListener {
            findNavController().navigate(R.id.action_Nav_to_News)
        }
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }
}