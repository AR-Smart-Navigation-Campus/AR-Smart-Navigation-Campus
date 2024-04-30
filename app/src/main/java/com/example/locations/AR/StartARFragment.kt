package com.example.locations.AR

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.StartArFragmentBinding
import com.google.ar.sceneform.ux.ArFragment

class StartARFragment : Fragment() {

    private lateinit var binding : StartArFragmentBinding
    private lateinit var arFragment: ArFragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StartArFragmentBinding.inflate(inflater, container, false)
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StartArFragmentBinding.bind(view)
        arFragment = childFragmentManager.findFragmentById(R.id.sceneView) as ArFragment
        arFragment.planeDiscoveryController.show()
    }

    fun test(){

    }

}