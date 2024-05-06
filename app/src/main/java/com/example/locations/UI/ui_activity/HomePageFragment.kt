package com.example.locations.UI.ui_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.HomePageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class HomePageFragment: Fragment() {
    lateinit var binding : HomePageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = HomePageBinding.inflate(inflater, container, false)


            val window: Window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(requireContext(), androidx.cardview.R.color.cardview_dark_background)


        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_loginFragment)
        }

        binding.buttonRegister.setOnClickListener{
            findNavController().navigate(R.id.action_homePage_to_Register)
        }

        binding.buttonAdmin.setOnClickListener{
            findNavController().navigate(R.id.action_homePage_to_addLocationFragment)
        }
        auth = Firebase.auth // Initialize Firebase Auth
        return binding.root
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToHomePage()
        }
    }
    private fun navigateToHomePage() {
        view?.let {
            Navigation.findNavController(it).navigate(R.id.action_homePage_to_Nav)
        }
    }
}