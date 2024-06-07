package com.example.locations.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.NewsletterFragmentBinding

class NewsletterFragment : Fragment() {

    private lateinit var binding: NewsletterFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewsletterFragmentBinding.inflate(inflater, container, false)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_News_to_Nav)
            }

        })
        return binding.root
    }

}