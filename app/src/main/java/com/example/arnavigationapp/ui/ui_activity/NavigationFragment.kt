package com.example.arnavigationapp.ui.ui_activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
    private lateinit var binding: NavigationFragmentBinding
    private var auth: FirebaseAuth = Firebase.auth
    private val currentUser = auth.currentUser
    private var isNavigationStarted = false

    // Register a launcher for requesting location permission.
    private val locationRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            handleLocationPermissionResult(it)
        }

    // Inflate the layout for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NavigationFragmentBinding.inflate(inflater, container, false)

        val moveUpAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.to_up)
        val moveDownAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.move_down)
        val fadeInAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val fadeOutAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)

        binding.btnStartNav.setOnClickListener {
            if (!isNavigationStarted) {
                binding.btnStartNav.startAnimation(moveUpAnimation)
                moveUpAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        binding.btnStartNav.visibility = View.GONE
                        binding.btnMap.visibility = View.VISIBLE
                        binding.btnList.visibility = View.VISIBLE
                        binding.btnMap.startAnimation(fadeInAnimation)
                        binding.btnList.startAnimation(fadeInAnimation)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            } else {
                binding.btnMap.startAnimation(fadeOutAnimation)
                binding.btnList.startAnimation(fadeOutAnimation)
                fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        binding.btnMap.visibility = View.GONE
                        binding.btnList.visibility = View.GONE
                        binding.btnStartNav.visibility = View.VISIBLE
                        binding.btnStartNav.startAnimation(moveDownAnimation)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
            isNavigationStarted = !isNavigationStarted
        }
        binding.btnNews.setOnClickListener {
            val url = "https://www.hit.ac.il/news/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(url)
            startActivity(intent)
        }

        val buttonMenu: ImageButton = binding.buttonMenu
        buttonMenu.setOnClickListener {
            showPopupMenu(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                @SuppressLint("ResourceAsColor")
                override fun handleOnBackPressed() {
                    val alertDialog =
                        AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
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

        binding.btnList.setOnClickListener {
            val bundle = bundleOf("returnToFragmentId" to R.id.action_allLocationsFragments_to_Nav)
            findNavController().navigate(R.id.action_Nav_to_allLocationsFragments, bundle)
        }

        binding.btnMap.setOnClickListener {
            findNavController().navigate(R.id.action_Nav_to_Map)
        }

        return binding.root
    }

    // Check and request location permission.
    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Handle the location permission result.
    private fun handleLocationPermissionResult(isGranted: Boolean) {
        if (isGranted)
            Toast.makeText(
                requireContext(),
                getString(R.string.location_permission_msg),
                Toast.LENGTH_SHORT
            ).show()
    }

    // Show the popup menu.
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.menu)
        popupMenu.menu.findItem(R.id.action_add_location).isVisible =
            currentUser != null && currentUser.email == viewModel.admin
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_add_location -> {
                    // Handle add location
                    findNavController().navigate(R.id.action_Nav_to_addItemFragment)
                    true
                }

                R.id.action_logout -> {
                    // Handle logout
                    val alertDialog =
                        AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                            .setTitle(getString(R.string.confirm_logout))
                            .setMessage(getString(R.string.logout_msg_text))
                            .setPositiveButton(getString(R.string.yes), null)
                            .setNegativeButton(getString(R.string.no), null)
                            .create()
                    alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                    alertDialog.setOnShowListener {
                        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        // Change the color of the "Yes" button
                        positiveButton.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.black
                            )
                        )

                        val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        // Change the color of the "No" button
                        negativeButton.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.black
                            )
                        )

                        positiveButton.setOnClickListener {
                            alertDialog.dismiss()
                            auth = Firebase.auth
                            Firebase.auth.signOut()
                            findNavController().navigate(R.id.action_Nav_to_homepage)
                        }
                    }

                    alertDialog.show()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }
}