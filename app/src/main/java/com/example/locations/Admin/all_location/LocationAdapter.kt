package com.example.locations.Admin.all_location

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.databinding.LocationItemBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Adapter for the RecyclerView in AllLocationsFragment
class LocationAdapter(
    // List of locations to display
    private var locationList: List<LocationData>,
    // Callback to handle item click and long click events
    val callBack: ItemListener
) :
RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    // Interface for item click and long click events
    interface ItemListener {
        fun onItemClick(index:Int)
        fun onItemLongClicked(index:Int)
    }

    // ViewHolder for the RecyclerView items
    inner class LocationViewHolder(private val binding: LocationItemBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener , View.OnLongClickListener {

        // Set click listeners for the item view
        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        // Handle item click event
        override fun onClick(v: View?) {
            callBack.onItemClick(adapterPosition)
        }

        // Handle item long click event
        override fun onLongClick(v: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition)
            return true
        }

        // Bind the location data to the item view
        fun bind(location: LocationData) {
            val auth=Firebase.auth
            val currentUser=auth.currentUser?.email.toString()
            val admin="navigationproject2024@gmail.com"
            val coords=binding.locationTextView
            val azimuth=binding.azimuthTextView
            coords.append(location.location)
            azimuth.append(location.azimuth)
            binding.buildingName.text=location.text
            if(currentUser==admin) {
            azimuth.visibility=View.VISIBLE
                coords.visibility=View.VISIBLE
            }

            Glide.with(binding.root).load(location.img).into(binding.buildingImage)
        }
    }

    // Get location at a specific position
    fun itemAt(position: Int) = locationList[position]

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding =
            LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    // Bind location to ViewHolder
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val currentItem = locationList[position]
        holder.bind(currentItem)
    }

    // Get the size of location list
    override fun getItemCount() = locationList.size

    // Update the location list and notify the adapter to refresh the RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<LocationData>) {
        locationList = newList
        notifyDataSetChanged()
    }
}