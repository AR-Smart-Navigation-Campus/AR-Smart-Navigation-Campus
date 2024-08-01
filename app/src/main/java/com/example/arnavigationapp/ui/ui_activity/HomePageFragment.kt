package com.example.arnavigationapp.ui.ui_activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.arnavigationapp.R
import com.example.arnavigationapp.databinding.HomePageBinding
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
        auth = FirebaseAuth.getInstance()
        welcomeMessage()
        setupButtonListeners()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                @SuppressLint("ResourceAsColor")
                override fun handleOnBackPressed() {
                    if (binding.RegisterCardView.visibility == View.VISIBLE || binding.SignInCardView.visibility == View.VISIBLE) {
                        binding.RegisterCardView.visibility = View.GONE
                        binding.SignInCardView.visibility = View.GONE
                        binding.buttonRegister.visibility = View.VISIBLE
                        binding.buttonLogin.visibility = View.VISIBLE
                        clearInput()
                    } else requireActivity().finish()
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
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) // Get the current hour
        var welcomeMessage = when {
            currentHour < 12 -> getString(R.string.morning_msg)
            currentHour < 18 -> getString(R.string.afternoon_msg)
            else -> getString(R.string.evening_msg)
        }
        welcomeMessage += '\n' + getString(R.string.signIn_Or_Create_Account)
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

    // Login function
    private fun loginFunc() {
        val txtEmail = binding.emailTextInput.editText?.text.toString()
        val txtPass = binding.passwordTextInput.editText?.text.toString()
        if (txtEmail.isNotEmpty() && txtPass.isNotEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(txtEmail, txtPass) // Sign in with email and password
                .addOnCompleteListener(requireActivity()) { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        logintoggleValidationError(false)
                        navigateToHomePage()
                    } else {
                        logintoggleValidationError(true)
                    }
                }
        } else {
            logintoggleValidationError(true)
        }
    }

    // Register a new user
    private fun regFunc() {
        val email = binding.regEmailTextInput
        val password = binding.regPasswordTextInput
        val confirmPass = binding.regConfirmPasswordTextInput
        val txtEmail = email.editText?.text.toString()
        val txtPass = password.editText?.text.toString()
        val txtConfirmPass = confirmPass.editText?.text.toString()
        if (txtEmail.isNotEmpty() && txtPass.isNotEmpty()) {
            regtoggleValidationError(false)
            if (txtPass != txtConfirmPass) {
                togglePassError(true)
            } else {
                togglePassError(false)
                auth.createUserWithEmailAndPassword(txtEmail, txtPass)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            regtoggleValidationError(false)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.registration_success),
                                Toast.LENGTH_LONG
                            ).show()
                            navigateToHomePage()
                        } else regtoggleValidationError(true)
                    }
            }
        } else {
            regtoggleValidationError(true)
        }
    }

    // Toggle the error state of the password input field
    private fun togglePassError(isError: Boolean) {
        val password = binding.regConfirmPasswordTextInput
        if (isError) {
            password.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
            password.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.red)
            password.setStartIconTintList(
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            )
            binding.passError.visibility = View.VISIBLE
            val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.vibrate)
            password.startAnimation(shakeAnimation)
        } else {
            password.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.black)
            password.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            password.setStartIconTintList(
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))
            )
            binding.passError.visibility = View.GONE
        }
    }

    // Toggle validation error
    private fun logintoggleValidationError(isError: Boolean) {
        val email = binding.emailTextInput
        val password = binding.passwordTextInput
        if (isError) {
            email.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
            email.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.red)
            email.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
            )
            binding.loginError.visibility = View.VISIBLE
            password.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
            password.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.red)
            password.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
            )
            val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.vibrate)
            email.startAnimation(shakeAnimation)
            password.startAnimation(shakeAnimation)
        } else {
            email.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.black)
            email.hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.black)
            email.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            )
            binding.loginError.visibility = View.GONE
            password.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.black)
            password.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            password.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            )
        }
    }

    // Toggle the error state of the email input field
    private fun regtoggleValidationError(isError: Boolean) {
        val email = binding.regEmailTextInput
        val password = binding.regPasswordTextInput
        if (isError) {
            email.boxStrokeColor = ContextCompat.getColor(
                requireContext(),
                R.color.red
            ) // Set the box stroke color to red
            email.hintTextColor = ContextCompat.getColorStateList(
                requireContext(),
                R.color.red
            ) // Set the hint text color to red
            email.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
            ) // Set the start icon tint list to red
            binding.regError.visibility = View.VISIBLE
            password.boxStrokeColor = ContextCompat.getColor(
                requireContext(),
                R.color.red
            ) // Set the box stroke color to red
            password.hintTextColor = ContextCompat.getColorStateList(
                requireContext(),
                R.color.red
            ) // Set the hint text color to red
            password.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
            ) // Set the start icon tint list to red
            val shakeAnimation =
                AnimationUtils.loadAnimation(context, R.anim.vibrate) // Load the vibrate animation
            password.startAnimation(shakeAnimation) // Start the vibrate animation
            email.startAnimation(shakeAnimation) // Start the vibrate animation
        } else {
            binding.regEmailTextInput.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            binding.regEmailTextInput.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.regEmailTextInput.setStartIconTintList(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            )
            binding.regError.visibility = View.GONE
            binding.regPasswordTextInput.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            binding.regPasswordTextInput.hintTextColor =
                ContextCompat.getColorStateList(requireContext(), R.color.black)
            binding.regPasswordTextInput.setStartIconTintList(
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))
            )
        }
    }

    // Clear the input fields
    private fun clearInput() {
        binding.emailTextInput.editText?.text?.clear()
        binding.passwordTextInput.editText?.text?.clear()
        binding.regEmailTextInput.editText?.text?.clear()
        binding.regPasswordTextInput.editText?.text?.clear()
        binding.regConfirmPasswordTextInput.editText?.text?.clear()
    }

    // Set up button listeners
    private fun setupButtonListeners() {
        binding.buttonLogin.setOnClickListener {
            binding.RegisterCardView.visibility = View.GONE
            binding.SignInCardView.visibility = View.VISIBLE
            binding.buttonRegister.visibility = View.GONE
            binding.buttonLogin.visibility = View.GONE
        }
        binding.buttonRegister.setOnClickListener {
            binding.SignInCardView.visibility = View.GONE
            binding.RegisterCardView.visibility = View.VISIBLE
            binding.buttonRegister.visibility = View.GONE
            binding.buttonLogin.visibility = View.GONE
        }
        binding.confirmLogin.setOnClickListener {
            view?.let { loginFunc() }
        }
        binding.SignInToRegisterButton.setOnClickListener {
            binding.SignInCardView.visibility = View.GONE
            binding.RegisterCardView.visibility = View.VISIBLE
            clearInput()
        }
        binding.RegisterToSignInButton.setOnClickListener {
            binding.RegisterCardView.visibility = View.GONE
            binding.SignInCardView.visibility = View.VISIBLE
            clearInput()
        }
        binding.confirmRegister.setOnClickListener {
            view?.let { regFunc() }
        }
        binding.buttonHomepageFromLogin.setOnClickListener {
            binding.SignInCardView.visibility = View.GONE
            binding.buttonRegister.visibility = View.VISIBLE
            binding.buttonLogin.visibility = View.VISIBLE
            clearInput()
        }
        binding.buttonHomepageFromRegister.setOnClickListener {
            binding.RegisterCardView.visibility = View.GONE
            binding.buttonRegister.visibility = View.VISIBLE
            binding.buttonLogin.visibility = View.VISIBLE
            clearInput()
        }
    }
}
