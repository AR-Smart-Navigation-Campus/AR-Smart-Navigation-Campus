package com.example.locations.UI

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.RegisterFragmentBinding
import kotlinx.android.parcel.Parcelize

class RegisterFragment : Fragment() {

    data class User(
        val email: String,
        val username: String,
        val password: String
    )

    private lateinit var binding: RegisterFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterFragmentBinding.inflate(inflater, container, false)
        binding.buttonHomepage.setOnClickListener {
            findNavController().navigate(R.id.action_RegisterFragment_to_homePage)
        }
        val users= mutableListOf<User>()

        binding.confirmRegister.setOnClickListener {
            if(regCheck()){
               // users.add(User(email, username, password))
                findNavController().navigate(R.id.action_RegisterFragment_to_homePage)
            }

        }

        return binding.root

    }
    fun regCheck(): Boolean {

        var email = binding.emailTextInput.editText?.text.toString()
        var username = binding.regUsernameTextInput.editText?.text.toString()
        var password = binding.regPasswordTextInput.editText?.text.toString()
        var confirmPassword = binding.regConfirmPasswordTextInput.editText?.text.toString()
        // Check if the fields are not empty
        if (email.isEmpty()) {
            binding.emailTextInput.error = "Email is required"
            return false
        }else{
            binding.emailTextInput.error = null
        }

        if (username.isEmpty()) {
            binding.regUsernameTextInput.error = "Username is required"
            return false
        }else{
            binding.regUsernameTextInput.error = null
        }

        if (password.isEmpty()) {
            binding.regPasswordTextInput.error = "Password is required"
            return false
        }else{
            binding.regPasswordTextInput.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.regConfirmPasswordTextInput.error = "password confirmation is required"
            return false
        }else{
            binding.regConfirmPasswordTextInput.error = null
        }

        // Check if the email is valid
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTextInput.error = "Invalid email"
            return false
        }else{
            binding.emailTextInput.error = null
        }

        // Check if the password and confirm password fields match
        if (password != confirmPassword) {
            binding.regConfirmPasswordTextInput.error = "Passwords do not match"
            return false
        }else{
            binding.regConfirmPasswordTextInput.error = null
        }

        // If all checks pass, return true
        return true
    }
}
