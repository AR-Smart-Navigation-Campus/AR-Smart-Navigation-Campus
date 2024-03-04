package com.example.locations

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.locations.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels()

    private val locationRequestLauncher : ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){
            getLocationUpdates()
        }
        val no = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocationUpdates()
        }
        else{
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.btnSavePlace.setOnClickListener {
            val latitude = MainActivityViewModel.data.latitude
            val longitude = MainActivityViewModel.data.longitude
            val name = binding.placeName.text.toString()

            viewModel.addData(latitude, longitude ,name)
            binding.placeName.text.clear()
        }

        viewModel.placeData.observe(this){ list-> // problem - not showing the list
            if(list.isNotEmpty()){
                binding.dataView.text = list.last().toString()

            }
        }
    }

    private fun getLocationUpdates(){
        viewModel.address.observe(this){
            binding.textView.text = it
        }
    }
}