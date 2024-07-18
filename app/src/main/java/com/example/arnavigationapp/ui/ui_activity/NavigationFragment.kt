package com.example.arnavigationapp.ui.ui_activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.arnavigationapp.admin.all_location.AdminViewModel
import com.example.arnavigationapp.R
import com.example.arnavigationapp.databinding.NavigationFragmentBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

/**
 * A fragment representing the navigation screen.
 */

class NavigationFragment : Fragment() {

    private val viewModel: AdminViewModel by activityViewModels() // Instance of AdminViewModel to access the data
    private lateinit var binding : NavigationFragmentBinding
    private lateinit var auth: FirebaseAuth

    // Register a launcher for requesting location permission.
    private val locationRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { handleLocationPermissionResult(it) }

    // Inflate the layout for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NavigationFragmentBinding.inflate(inflater, container, false)
        binding.btnStartNav.setOnClickListener {
            findNavController().navigate(R.id.action_Nav_to_StartNav)
        }
        binding.btnNews.setOnClickListener {
            val url="https://www.hit.ac.il/news/"
            val intent=Intent(Intent.ACTION_VIEW)
            intent.data= android.net.Uri.parse(url)
            startActivity(intent)
        }
        binding.btnLogout.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                .setTitle(getString(R.string.confirm_logout))
                .setMessage(getString(R.string.logout_msg_text))
                .setPositiveButton(getString(R.string.yes), null)
                .setNegativeButton(getString(R.string.no), null)
                .create()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            alertDialog.setOnShowListener {
                val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // Change the color of the "Yes" button

                val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // Change the color of the "No" button

                positiveButton.setOnClickListener {
                    alertDialog.dismiss()
                    auth = Firebase.auth
                    Firebase.auth.signOut()
                    findNavController().navigate(R.id.action_Nav_to_homepage)
                }
            }

            alertDialog.show()

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            @SuppressLint("ResourceAsColor")
            override fun handleOnBackPressed() {
                val alertDialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                    .setTitle(getString(R.string.confirm_exit))
                    .setMessage(getString(R.string.exit_msg_text))
                    .setPositiveButton(getString(R.string.yes), null)
                    .setNegativeButton(getString(R.string.no), null)
                    .create()
                alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                alertDialog.setOnShowListener {
                    val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setTextColor(R.color.black) // Change the color of the "Yes" button

                    val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    negativeButton.setTextColor(R.color.black) // Change the color of the "No" button

                    positiveButton.setOnClickListener {
                        alertDialog.dismiss()
                        requireActivity().finish()
                    }
                }

                alertDialog.show()
            }
        })

        checkAndRequestLocationPermission()

        return binding.root
    }

    // Check if the user is an admin and set the button visibility accordingly.
    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser!=null && currentUser.email== viewModel.admin){
            binding.buttonAdmin.visibility = View.VISIBLE
            binding.buttonAdmin.setOnClickListener {
                findNavController().navigate(R.id.action_Nav_to_addItemFragment)
            }
        }
    }

    // Check and request location permission.
    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Handle the location permission result.
    private fun handleLocationPermissionResult(isGranted: Boolean) {
        if (isGranted)
            Toast.makeText(requireContext(), getString(R.string.location_permission_msg), Toast.LENGTH_SHORT).show()
    }
}