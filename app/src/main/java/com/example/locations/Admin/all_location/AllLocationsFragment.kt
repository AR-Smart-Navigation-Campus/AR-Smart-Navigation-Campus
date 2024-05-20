package com.example.locations.Admin.all_location

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locations.R
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.databinding.AllLocationsLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class AllLocationsFragment : Fragment() {

    private var _binding: AllLocationsLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private val admin = "navigationproject2024@gmail.com"
    private val viewModel: AdminViewModel by activityViewModels()
    private lateinit var adapter: LocationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AllLocationsLayoutBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser?.email.toString()
        setupRecyclerView(currentUser)

        binding.btnBack.setOnClickListener { returnToAdd() }

        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray))
        binding.searchEditTextLayout.defaultHintTextColor = colorStateList

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnToAdd()
            }
        })

        return binding.root
    }

    private fun returnToAdd() {
        val returnToFragmentId = arguments?.getInt("returnToFragmentId")
        if (returnToFragmentId != null) {
            findNavController().navigate(returnToFragmentId)
        } else {
            findNavController().navigate(R.id.action_allLocationsFragments_to_StartNav)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchEditText()
    }

    private fun setupRecyclerView(currentUser: String) {
        viewModel.locationData.observe(viewLifecycleOwner) { allLocations ->
            setupAdapter(allLocations)
        }
    }

    private fun setupAdapter(allLocations: List<LocationData>) {
        adapter = LocationAdapter(allLocations, object : LocationAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                viewModel.setLocation(allLocations[index])
                binding.searchEditTextLayout.editText?.text?.clear()
                findNavController().navigate(R.id.action_allLocationsFragments_to_AR)
            }

            override fun onItemLongClicked(index: Int) {
                viewModel.setLocation(allLocations[index])
                binding.searchEditTextLayout.editText?.text?.clear()
                findNavController().navigate(R.id.action_allLocationsFragments_to_detailLocationInfo)
            }
        })
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearchEditText() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterBuilding(s.toString())
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun filterBuilding(query: String) {
        if (!::adapter.isInitialized) return
        viewModel.locationData.observe(viewLifecycleOwner) { allLocations ->
            val filteredList = if (query.isNotEmpty()) {
                allLocations.filter { it.name.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) }
            } else {
                allLocations
            }
            adapter.updateData(filteredList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
