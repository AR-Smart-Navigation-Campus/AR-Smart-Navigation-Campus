package com.example.locations.ui.ui_activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.HomePageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.util.Calendar

/**
 * Fragment class for the home page.
 *
 */

class HomePageFragment : Fragment() {
    lateinit var binding: HomePageBinding
    private lateinit var auth: FirebaseAuth

    // Function to inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = HomePageBinding.inflate(inflater, container, false)
        val window: Window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(
            requireContext(),
            androidx.cardview.R.color.cardview_dark_background
        )

        welcomeMessage()
        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_loginFragment)
        }

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_Register)
        }

        auth = Firebase.auth // Initialize Firebase Auth

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                @SuppressLint("ResourceAsColor")
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
        return binding.root
    }

    // Function to check if the user is already logged in
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser

        if (currentUser != null) {
            navigateToHomePage()

        }
    }

    // Function to navigate to the home page
    private fun navigateToHomePage() {
        view?.let {
            Navigation.findNavController(it).navigate(R.id.action_homePage_to_Nav)
        }
    }

    // Function to display a welcome message based on the current time
    private fun welcomeMessage() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val welcomeMessage = when {
            currentHour < 12 -> getString(R.string.morning_msg)
            currentHour < 18 -> getString(R.string.afternoon_msg)
            else -> getString(R.string.evening_msg)
        }

        val textView = binding.welcomeText
        val handler = Handler(Looper.getMainLooper())
        var index = 0

        val runnable = object : Runnable {
            override fun run() {
                if (index < welcomeMessage.length) {
                    textView.text = buildString {
                        append(textView.text.toString())
                        append(welcomeMessage[index])
                    }
                    index++
                    handler.postDelayed(this, 25) // delay of 25ms
                }
            }
        }
        handler.post(runnable)
    }
}