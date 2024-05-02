package com.example.locations.UI

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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

        // Clear focus and close keyboard when the user presses the "Done" or "Next" button on the keyboard
        binding.passwordTextInput.editText?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                // Clear focus
                v.clearFocus()

                // Close keyboard
                val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                true
            } else {
                false
            }
        }
        binding.confirmLogin.setOnClickListener{
           if(checkLogin()) {
               findNavController().navigate(R.id.action_LoginFragment_to_Nav)
           }
        }
        return binding.root
    }
fun checkLogin():Boolean {
    val username = binding.usernameTextInput.editText?.text.toString()
    val password = binding.passwordTextInput.editText?.text.toString()
    if (username.isEmpty()) {
        binding.usernameTextInput.error = "Username is required"
        return false
    } else {
        binding.usernameTextInput.error = null
    }
    if (password.isEmpty()) {
        binding.passwordTextInput.error = "Password is required"
        return false
    } else {
        binding.passwordTextInput.error = null
      }
   return true
}
}