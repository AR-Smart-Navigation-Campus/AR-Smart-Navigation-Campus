package com.example.locations.UI

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.LoginFragmentBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginFragment : Fragment() {

    lateinit var binding : LoginFragmentBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)

        binding.buttonHomepage.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_homePage)
        }
        binding.confirmLogin.setOnClickListener{
            view?.let { it1 -> loginFunc(it1) }
        }
        binding.registerButton.setOnClickListener{
            findNavController().navigate(R.id.action_LoginFragment_to_Register)
        }
        auth = Firebase.auth // Initialize Firebase Auth
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_loginFragment_to_homePage)
            }

        })

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
            Navigation.findNavController(it).navigate(R.id.action_LoginFragment_to_Nav)
        }
    }
    private fun loginFunc(view: View) {
       val txtEmail=binding.emailTextInput.editText?.text.toString()
        val txtPass=binding.passwordTextInput.editText?.text.toString()
        if (txtEmail.isNotEmpty() && txtPass.isNotEmpty()) {
            auth.signInWithEmailAndPassword(txtEmail, txtPass)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        toggleValidationError(false)
                        Toast.makeText(requireContext(), "Login Success!", Toast.LENGTH_SHORT).show()
                        navigateToHomePage()
                    } else {
                        toggleValidationError(true)
                    }
                }
        } else {
            toggleValidationError(true)
        }
    }

    private fun toggleValidationError(isError: Boolean) {
        var email = binding.emailTextInput
        var password = binding.passwordTextInput
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
            val shakeAnimation= AnimationUtils.loadAnimation(context, R.anim.vibrate)
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