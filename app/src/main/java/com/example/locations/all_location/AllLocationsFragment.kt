package com.example.locations.all_location

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locations.MainViewModel
import com.example.locations.R
import com.example.locations.data.model.LocationData
import com.example.locations.databinding.AllLocationsLayoutBinding
import java.util.Locale

class AllLocationsFragment : Fragment() {

    private var _binding: AllLocationsLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LocationAdapter

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?):
            View {
        _binding = AllLocationsLayoutBinding.inflate(inflater, container, false)
        setupRecyclerView()
        binding.buttonBack.setOnClickListener {
            binding.searchEditText.text.clear()
            findNavController().navigate(R.id.action_allLocationsFragments_to_addLocationFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchEditText()
    }

    private fun setupRecyclerView() {
        viewModel.locationData?.observe(viewLifecycleOwner) { allLocations ->
            setupAdapter(allLocations)
            setupItemTouchHelper()
        }
    }

    private fun setupAdapter(allLocations: List<LocationData>) {
         adapter = LocationAdapter(allLocations, object : LocationAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                Toast.makeText(requireContext(), "Long touch for detailed info", Toast.LENGTH_SHORT).show()
            }

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

    private fun setupItemTouchHelper() {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
                makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val location = (binding.recyclerView.adapter as LocationAdapter).itemAt(viewHolder.adapterPosition)
                viewModel.deleteEntry(location)
            }
        }).attachToRecyclerView(binding.recyclerView)
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

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }