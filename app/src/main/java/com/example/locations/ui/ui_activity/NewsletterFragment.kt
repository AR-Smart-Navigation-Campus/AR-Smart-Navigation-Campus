package com.example.locations.ui.ui_activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.NewsletterFragmentBinding

/**
 *  NewsletterFragment class that extends Fragment and provides functionality for the newsletter screen.
 */

class NewsletterFragment : Fragment() {

    private lateinit var binding: NewsletterFragmentBinding

    // Inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewsletterFragmentBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_News_to_Nav)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_News_to_Nav)
            }

        })
        return binding.root
    }

}