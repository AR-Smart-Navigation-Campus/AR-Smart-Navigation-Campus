package com.example.locations.Admin.all_location

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.databinding.LocationItemBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Adapter for the RecyclerView in AllLocationsFragment
 */

class LocationAdapter(
    // List of locations to display
    private var locationList: List<LocationData>,
    // Callback to handle item click and long click events
    val callBack: ItemListener
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    // Interface for item click and long click events
    interface ItemListener {
        fun onItemClick(index: Int)
        fun onItemLongClicked(index: Int)
    }

    // ViewHolder for the RecyclerView items
    inner class LocationViewHolder(private val binding: LocationItemBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

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
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser?.email.toString()
            val admin = "navigationproject2024@gmail.com"
            binding.buildingName.text = location.name
            Glide.with(binding.root).load(location.imgUrl).circleCrop().into(binding.buildingImage)
            val coords = binding.locationTextView
            val azimuth = binding.azimuthTextView
            coords.text = location.location
            azimuth.text = location.azimuth
            if (currentUser != admin) {
                coords.visibility = View.GONE
                azimuth.visibility = View.GONE
            }
        }
    }

    fun itemAt(position: Int) = locationList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val currentItem = locationList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = locationList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<LocationData>) {
        locationList = newList
        notifyDataSetChanged()
    }
}
