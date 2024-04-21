package com.example.locations.all_location

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.locations.data.model.LocationData
import com.example.locations.databinding.LocationItemBinding

class LocationAdapter(private var locationList: List<LocationData>,val callBack: ItemListener) :
RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {


    interface ItemListener {
        fun onItemClick(index:Int)
        fun onItemLongClicked(index:Int)
    }

    inner class LocationViewHolder(private val binding: LocationItemBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener , View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            callBack.onItemClick(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            callBack.onItemLongClicked(adapterPosition)
            return true
        }

        fun bind(location: LocationData) {
            binding.buildingName.text = location.text
            binding.locationTextView.text = location.location
            binding.azimuthTextView.text = location.azimuth
            Glide.with(binding.root).load(location.img).circleCrop().into(binding.buildingImage)
        }
    }
        fun itemAt(position: Int) = locationList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding =
            LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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