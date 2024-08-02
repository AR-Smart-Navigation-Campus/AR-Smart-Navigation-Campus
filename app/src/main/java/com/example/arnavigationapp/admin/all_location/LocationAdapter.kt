package com.example.arnavigationapp.admin.all_location

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.arnavigationapp.admin.all_location.model.LocationData
import com.example.arnavigationapp.R
import com.example.arnavigationapp.databinding.LocationItemBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Adapter for the RecyclerView in AllLocationsFragment
 */

class LocationAdapter(
    locationList: List<LocationData>, // List of locations to display
    val callBack: ItemListener, // Callback to handle item click and long click events
    private val viewModel: AdminViewModel // Add ViewModel as a constructor parameter

) :
    RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    var currentList: List<LocationData> = locationList

    // Interface for item click and long click events
    interface ItemListener {
        fun onItemClick(index: Int)
        fun onItemLongClicked(index: Int)
    }

    // ViewHolder for the RecyclerView items
    inner class LocationViewHolder(private val binding: LocationItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        // Set click listeners for the item view
        init {
            binding.root.setOnClickListener(this) // Handle item click event
            binding.root.setOnLongClickListener(this) // Handle item long click event
        }

        // Handle item click event
        override fun onClick(v: View?) {
            callBack.onItemClick(adapterPosition) // Notify the callback
        }

        // Handle item long click event
        override fun onLongClick(v: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition) // Notify the callback
            return true
        }

        // Bind the location data to the item view
        fun bind(location: LocationData) {
            val auth = Firebase.auth
            val currentUser = auth.currentUser?.email.toString()
            val resId = viewModel.getLocationNameResId(location.name)
            if (resId == R.string.unknown_location) {
                binding.buildingName.text = location.name
            } else {
                binding.buildingName.text = binding.root.context.getString(resId)
            }
            Glide.with(binding.root).load(location.imgUrl).circleCrop().into(binding.buildingImage)
            val coords = binding.locationTextView
            val azimuth = binding.azimuthTextView
            val description = binding.descriptionTextView
            val coordsText =
                binding.root.context.getString(R.string.coordinates) + ": " + location.location.replace(
                    "\\s+".toRegex(),
                    ""
                )
            coords.text = coordsText
            val azimuthText =
                binding.root.context.getString(R.string.azimuth) + ": " + location.azimuth
            azimuth.text = azimuthText
            if (currentUser != viewModel.admin) {
                coords.visibility = View.GONE
                azimuth.visibility = View.GONE
                val descriptionId = viewModel.getLocationDescriptionResId(location.description)
                if (descriptionId == R.string.no_desc) {
                    description.text = location.description
                } else {
                    description.text = binding.root.context.getString(descriptionId)
                }
                description.visibility = View.VISIBLE
            }
        }
    }

    // Get location at a specific position
    fun itemAt(position: Int) = currentList[position]

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding =
            LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding) // Create and return a new ViewHolder
    }

    // Bind location to ViewHolder
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.bind(currentItem) // Bind the location data to the ViewHolder
    }

    // Get the size of location list
    override fun getItemCount() = currentList.size

    // Update the location list and notify the adapter to refresh the RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<LocationData>) {
        currentList = newList
        notifyDataSetChanged()
    }

}