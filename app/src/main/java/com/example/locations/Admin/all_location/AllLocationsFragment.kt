package com.example.locations.Admin.all_location

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
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
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

/**
 * Fragment for displaying all locations
 */
class AllLocationsFragment : Fragment() {

    private var _binding: AllLocationsLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private val admin = "navigationproject2024@gmail.com"
    private val viewModel: AdminViewModel by activityViewModels()
    private lateinit var adapter: LocationAdapter

    // Create view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AllLocationsLayoutBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance() // Get instance of Firebase authentication
        val currentUser = auth.currentUser?.email.toString()
        setupRecyclerView(currentUser) // Setup RecyclerView

        binding.btnBack.setOnClickListener { returnToAdd() }

        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray))
        binding.searchEditTextLayout.defaultHintTextColor = colorStateList

        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnToAdd()
            }
        })

        return binding.root
    }

    // Return to add location fragment
    private fun returnToAdd() {
        val returnToFragmentId = arguments?.getInt("returnToFragmentId")
        if (returnToFragmentId != null) {
            findNavController().navigate(returnToFragmentId)
        } else {
            findNavController().navigate(R.id.action_allLocationsFragments_to_StartNav)
        }
    }

    // Setup view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchEditText() // Setup search EditText
    }

    // Setup RecyclerView
    private fun setupRecyclerView(currentUser: String) {
        viewModel.locationData.observe(viewLifecycleOwner) { allLocations ->
            setupAdapter(allLocations) // Setup adapter for RecyclerView
            setupItemTouchHelper(currentUser) // Setup item touch helper for RecyclerView
        }
    }

    // Setup adapter for RecyclerView
    private fun setupAdapter(allLocations: List<LocationData>) {
        adapter = LocationAdapter(allLocations, object : LocationAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                viewModel.setLocation(allLocations[index])
                binding.searchEditTextLayout.editText?.text?.clear()
                findNavController().navigate(R.id.action_allLocationsFragments_to_AR)
            }

            override fun onItemLongClicked(index: Int) {
                viewModel.setLocation(allLocations[index]) // Set location entry
                binding.searchEditTextLayout.editText?.text?.clear()
                findNavController().navigate(R.id.action_allLocationsFragments_to_detailLocationInfo)
            }
        })
        binding.recyclerView.adapter = adapter // Set adapter for RecyclerView
        binding.recyclerView.itemAnimator = DefaultItemAnimator() // Set item animator for RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()) // Set layout manager for RecyclerView
    }

    // Setup item touch helper for RecyclerView
    private fun setupItemTouchHelper(currentUser:String) {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            // Setup movement flags for RecyclerView
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int{
                return if(currentUser==admin) {
                    makeFlag(
                        ItemTouchHelper.ACTION_STATE_SWIPE, // Swipe flag
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // Swipe direction
                    )
                }else{
                    0
                }
            }
            // Handle move event
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false // Unused
            // Handle swipe event
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(currentUser==admin) {
                    val location = (binding.recyclerView.adapter as LocationAdapter).itemAt(viewHolder.adapterPosition) // Get location entry
                    viewModel.deleteEntry(location) // Delete location entry
                }
            }
        }).attachToRecyclerView(binding.recyclerView) // Attach item touch helper to RecyclerView
    }

    // Setup search EditText
    private fun setupSearchEditText() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {} // Unused
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterBuilding(s.toString()) // Filter building based on query
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }


    // Filter building based on query
    private fun filterBuilding(query: String) {
        if (!::adapter.isInitialized) return
        viewModel.locationData.observe(viewLifecycleOwner) { allLocations ->
            val filteredList = if (query.isNotEmpty()) {
                allLocations.filter { it.name.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) }
            } else {
                allLocations
            }
            adapter.updateData(filteredList) // Update adapter with filtered data
        }
    }

    // Destroy view binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
