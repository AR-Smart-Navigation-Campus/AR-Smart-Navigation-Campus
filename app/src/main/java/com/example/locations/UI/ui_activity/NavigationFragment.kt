package com.example.locations.UI.ui_activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.NavigationFragmentBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class NavigationFragment : Fragment() {
    private lateinit var binding : NavigationFragmentBinding
    private lateinit var auth: FirebaseAuth

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
            findNavController().navigate(R.id.action_Nav_to_News)
        }
        binding.btnBack.setOnClickListener {
            auth = Firebase.auth
            Firebase.auth.signOut()
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            @SuppressLint("ResourceAsColor")
            override fun handleOnBackPressed() {
                val alertDialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                    .setTitle("Confirm Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", null)
                    .setNegativeButton("No", null)
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
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser!=null && currentUser.email== "navigationproject2024@gmail.com"){
            binding.buttonAdmin.visibility = View.VISIBLE
            binding.buttonAdmin.setOnClickListener {
                findNavController().navigate(R.id.action_Nav_to_addItemFragment)
            }
        }
    }

}