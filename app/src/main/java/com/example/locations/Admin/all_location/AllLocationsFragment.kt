package com.example.locations.Admin.all_location

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locations.R
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.databinding.AllLocationsLayoutBinding
import java.util.Locale

class AllLocationsFragment : Fragment() {

    // Binding object instance corresponding to the fragment_all_locations.xml layout
    private var _binding: AllLocationsLayoutBinding? = null

    // Non-null access to the binding object instance
    private val binding get() = _binding!!

    // Adapter for the RecyclerView
    private lateinit var adapter: LocationAdapter

    // ViewModel instance
    private val viewModel: AdminViewModel by activityViewModels()

    // Inflate the layout for this fragment
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?):
            View {
        _binding = AllLocationsLayoutBinding.inflate(inflater, container, false)
        // Setup RecyclerView
        setupRecyclerView()
        // Setup back button click listener

        return binding.root
    }

    // Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup search EditText
        setupSearchEditText()
    }

    // Setup RecyclerView with data from the ViewModel
    private fun setupRecyclerView() {
        viewModel.locationData?.observe(viewLifecycleOwner) { allLocations ->
            setupAdapter(allLocations)
            setupItemTouchHelper()
        }
    }

    // Setup adapter for RecyclerView
    private fun setupAdapter(allLocations: List<LocationData>) {
         adapter = LocationAdapter(allLocations, object : LocationAdapter.ItemListener {
             // Handle item click
            override fun onItemClick(index: Int) {
                 findNavController().navigate(R.id.action_allLocationsFragments_to_AR)
            }
             // Handle item long click
            override fun onItemLongClicked(index: Int) {
                viewModel.setLocation(allLocations[index])
                setupAdapter(allLocations)
                binding.searchEditText.text.clear()
                findNavController().navigate(R.id.action_allLocationsFragments_to_detailLocationInfo)
            }
        })
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    // Setup item touch helper for RecyclerView
    private fun setupItemTouchHelper() {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            // Setup movement flags for RecyclerView
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
                makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            // Handle move event
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            // Handle swipe event
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val location = (binding.recyclerView.adapter as LocationAdapter).itemAt(viewHolder.adapterPosition)
                viewModel.deleteEntry(location)
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    // Setup search EditText with text change listener
    private fun setupSearchEditText() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Handle text change
                filterBuilding(s.toString())
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    // Filter locations based on query
    private fun filterBuilding(query: String) {
        if (!::adapter.isInitialized) return
        val filteredList = ArrayList<LocationData>()
        viewModel.locationData?.observe(viewLifecycleOwner) { allLocations ->
            if (query.isNotEmpty()) {
                if (allLocations != null) {
                    for (building in allLocations) {
                        if (building.text.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(building)
                        }
                    }
                }
            } else {
                if (allLocations != null) {
                    filteredList.addAll(allLocations)
                }
            }
        }
        adapter.updateData(filteredList)
    }

    // Called when the view hierarchy associated with the fragment is being cleaned up
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }