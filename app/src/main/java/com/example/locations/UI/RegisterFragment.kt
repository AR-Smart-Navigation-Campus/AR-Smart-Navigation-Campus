package com.example.locations.UI

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.RegisterFragmentBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.android.parcel.Parcelize

class RegisterFragment : Fragment() {


    private lateinit var binding: RegisterFragmentBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterFragmentBinding.inflate(inflater, container, false)
        binding.buttonHomepage.setOnClickListener {
            findNavController().navigate(R.id.action_RegisterFragment_to_homePage)
        }


        binding.confirmRegister.setOnClickListener {
            view?.let { it1 -> regFunc(it1) }
        }

        return binding.root

    }
    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToHomePage()
        }
    }

    private fun navigateToHomePage() {
        Navigation.findNavController(requireView()).navigate(R.id.action_RegisterFragment_to_homePage)
    }

    private fun regFunc(view: View) {
        var email=binding.regEmailTextInput
        var password=binding.regPasswordTextInput
        var confirmPass=binding.regConfirmPasswordTextInput
        var txtEmail = email.editText?.text.toString()
        var txtPass =password.editText?.text.toString()
        var txtConfirmPass = confirmPass.editText?.text.toString()

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
                                "Registration Success",
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

    private fun togglePassError(isError: Boolean){
        if(isError) {
            binding.regConfirmPasswordTextInput.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
            binding.regConfirmPasswordTextInput.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.red)
            binding.regConfirmPasswordTextInput.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.red
                    )
                )
            )
            binding.passError.visibility = View.VISIBLE
        }else{
            binding.regConfirmPasswordTextInput.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            binding.regConfirmPasswordTextInput.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.regConfirmPasswordTextInput.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            )
            binding.passError.visibility = View.GONE
        }
    }

    private fun toggleValidationError(isError: Boolean){
        if(isError) {
            binding.regEmailTextInput.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
            binding.regEmailTextInput.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.red)
            binding.regEmailTextInput.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.red
                    )
                )
            )
            binding.regError.visibility = View.VISIBLE
            binding.regPasswordTextInput.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
            binding.regPasswordTextInput.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.red)
            binding.regPasswordTextInput.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.red
                    )
                )
            )
        }else{
            binding.regEmailTextInput.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            binding.regEmailTextInput.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.regEmailTextInput.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            )
            binding.regError.visibility = View.GONE
            binding.regPasswordTextInput.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            binding.regPasswordTextInput.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.regPasswordTextInput.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            )
        }
    }

}
