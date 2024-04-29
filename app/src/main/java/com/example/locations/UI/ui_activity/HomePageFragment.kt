package com.example.locations.UI.ui_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.HomePageBinding

class HomePageFragment: Fragment() {
    lateinit var binding : HomePageBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = HomePageBinding.inflate(inflater, container, false)

        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_loginFragment)
        }

        binding.buttonRegister.setOnClickListener{
            findNavController().navigate(R.id.action_homePage_to_Register)
        }

        binding.buttonAdmin.setOnClickListener{
            findNavController().navigate(R.id.action_homePage_to_addLocationFragment)
        }
        return binding.root
    }
}