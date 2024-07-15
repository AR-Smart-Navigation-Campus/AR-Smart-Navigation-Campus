package com.example.locations.ui.ui_activity

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.LoginFragmentBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

/**
 *  LoginFragment class that extends Fragment and provides UI for user login.
 */

class LoginFragment : Fragment() {

    lateinit var binding: LoginFragmentBinding
    private lateinit var auth: FirebaseAuth

    // Inflate the layout for this fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)

        binding.buttonHomepage.setOnClickListener {
            returnToHome()
        }
        binding.confirmLogin.setOnClickListener {
            view?.let { it1 -> loginFunc(it1) }
        }
        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_Register)
        }
        auth = Firebase.auth // Initialize Firebase Auth
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    returnToHome()
                }

            })

        return binding.root
    }

    // Return to the home page
    private fun returnToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_homePage)
    }

    // Check if user is signed in (non-null) and update UI accordingly.
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToHomePage()
        }
    }

    // Navigate to the home page
    private fun navigateToHomePage() {
        view?.let {
            Navigation.findNavController(it).navigate(R.id.action_LoginFragment_to_Nav)
        }
    }

    // Login function
    private fun loginFunc(view: View) {
        val txtEmail = binding.emailTextInput.editText?.text.toString()
        val txtPass = binding.passwordTextInput.editText?.text.toString()
        if (txtEmail.isNotEmpty() && txtPass.isNotEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(txtEmail, txtPass) // Sign in with email and password
                .addOnCompleteListener(requireActivity()) { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        toggleValidationError(false)
                        navigateToHomePage()
                    } else {
                        toggleValidationError(true)
                    }
                }
        } else {
            toggleValidationError(true)
        }
    }

    // Toggle validation error
    private fun toggleValidationError(isError: Boolean) {
        val email = binding.emailTextInput
        val password = binding.passwordTextInput
        if (isError) {
            email.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
            email.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.red)
            email.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.red
                    )
                )
            )
            binding.loginError.visibility = View.VISIBLE
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
            val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.vibrate)
            email.startAnimation(shakeAnimation)
            password.startAnimation(shakeAnimation)
        } else {
            email.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            email.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            email.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            )
            binding.loginError.visibility = View.GONE
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
        }
    }
}