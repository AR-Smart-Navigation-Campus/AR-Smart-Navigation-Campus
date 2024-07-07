package com.example.locations.Admin.all_location

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getString
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.locations.Admin.all_location.model.LocationData
import com.example.locations.R
import com.example.locations.databinding.LocationItemBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Adapter for the RecyclerView in AllLocationsFragment
 */

class LocationAdapter(
    private var locationList: List<LocationData>, // List of locations to display
    val callBack: ItemListener, // Callback to handle item click and long click events
    private val viewModel: AdminViewModel // Add ViewModel as a constructor parameter

) :
RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    var currentList: List<LocationData> = locationList

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
            val auth=Firebase.auth
            val currentUser=auth.currentUser?.email.toString()
            val admin="navigationproject2024@gmail.com"
            val resId = viewModel.getLocationNameResId(location.name)
            binding.buildingName.text = binding.root.context.getString(resId)
            Glide.with(binding.root).load(location.imgUrl).circleCrop().into(binding.buildingImage)
            val coords=   binding.locationTextView
            val azimuth= binding.azimuthTextView
            val description= binding.descriptionTextView

            val coordsText = binding.root.context.getString(R.string.coordinates) + ": " + location.location.replace("\\s+".toRegex(), "")
            coords.text = coordsText
            val azimuthText = binding.root.context.getString(R.string.azimuth) + ": " + location.azimuth
            azimuth.text = azimuthText

            if(currentUser!=admin) {
                coords.visibility=View.GONE
                azimuth.visibility=View.GONE
                val descriptionText =viewModel.getLocationDescriptionResId(location.description)
                description.text = binding.root.context.getString(descriptionText)
                description.visibility=View.VISIBLE
            }
        }
    }

    // Get location at a specific position
    fun itemAt(position: Int) = currentList[position]

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding =
            LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    // Bind location to ViewHolder
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.bind(currentItem)
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