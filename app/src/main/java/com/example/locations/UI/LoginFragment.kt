package com.example.locations.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.LoginFragmentBinding

class LoginFragment : Fragment() {

    lateinit var binding : LoginFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)

        binding.buttonHomepage.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_homePage)
        }

        binding.buttonlogin.setOnClickListener{
            findNavController().navigate(R.id.action_LoginFragment_to_Nav)
        }
        return binding.root
    }

}