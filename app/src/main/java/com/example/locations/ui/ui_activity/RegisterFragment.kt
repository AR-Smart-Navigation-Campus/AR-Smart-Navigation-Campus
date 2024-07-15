package com.example.locations.ui.ui_activity

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.RegisterFragmentBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

/**
 *  A fragment representing the registration screen.
 */

class RegisterFragment : Fragment() {

    private lateinit var binding: RegisterFragmentBinding
    private lateinit var auth: FirebaseAuth

    // Inflate the layout for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterFragmentBinding.inflate(inflater, container, false)
        binding.buttonHomepage.setOnClickListener {
            returnToHome()
        }

        binding.siginInButton.setOnClickListener{
            findNavController().navigate(R.id.action_RegisterFragment_to_LoginFragment)
        }
        binding.confirmRegister.setOnClickListener {
            view?.let { it1 -> regFunc() }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnToHome()
            }

        })
        return binding.root

    }

    // Return to the home page
    private fun returnToHome(){
        findNavController().navigate(R.id.action_RegisterFragment_to_homePage)
    }

    // Check if the user is already logged in and navigate to the home page
    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToHomePage()
        }
    }

    // Navigate to the home page
    private fun navigateToHomePage() {
        Navigation.findNavController(requireView()).navigate(R.id.action_RegisterFragment_to_homePage)
    }

    // Register a new user
    private fun regFunc() {
        val email=binding.regEmailTextInput
        val password=binding.regPasswordTextInput
        val confirmPass=binding.regConfirmPasswordTextInput
        val txtEmail = email.editText?.text.toString()
        val txtPass =password.editText?.text.toString()
        val txtConfirmPass = confirmPass.editText?.text.toString()

        if (txtEmail.isNotEmpty() && txtPass.isNotEmpty()) {
            toggleValidationError(false)
            if (txtPass != txtConfirmPass) {
                togglePassError(true)
            } else {
                togglePassError(false)
                auth.createUserWithEmailAndPassword(txtEmail, txtPass)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            toggleValidationError(false)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.registration_success),
                                Toast.LENGTH_LONG
                            ).show()
                            navigateToHomePage()
                        } else {
                            toggleValidationError(true)
                        }
                    }
            }
        } else {
            toggleValidationError(true)
        }
    }

    // Toggle the error state of the password input field
    private fun togglePassError(isError: Boolean){
        val password= binding.regConfirmPasswordTextInput
        if(isError) {
           password.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
            password.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.red)
            password.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.red
                    )
                )
            )
            binding.passError.visibility = View.VISIBLE
            val shakeAnimation= AnimationUtils.loadAnimation(context, R.anim.vibrate)
            password.startAnimation(shakeAnimation)
        }else{
            password.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            password.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            password.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            )
            binding.passError.visibility = View.GONE
        }
    }

    // Toggle the error state of the email input field
    private fun toggleValidationError(isError: Boolean){
        val email=binding.regEmailTextInput
        val password=binding.regPasswordTextInput
        if(isError) {
            email.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red) // Set the box stroke color to red
            email.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.red) // Set the hint text color to red
            email.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))) // Set the start icon tint list to red
            binding.regError.visibility = View.VISIBLE
            password.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red) // Set the box stroke color to red
            password.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.red) // Set the hint text color to red
            password.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))) // Set the start icon tint list to red
            val shakeAnimation= AnimationUtils.loadAnimation(context, R.anim.vibrate) // Load the vibrate animation
            password.startAnimation(shakeAnimation) // Start the vibrate animation
            email.startAnimation(shakeAnimation) // Start the vibrate animation
        }else{
            binding.regEmailTextInput.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.black)
            binding.regEmailTextInput.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.regEmailTextInput.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)))
            binding.regError.visibility = View.GONE
            binding.regPasswordTextInput.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.black)
            binding.regPasswordTextInput.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.regPasswordTextInput.setStartIconTintList(
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)))
        }
    }

}
