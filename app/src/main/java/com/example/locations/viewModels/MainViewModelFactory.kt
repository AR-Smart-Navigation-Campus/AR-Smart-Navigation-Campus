//package com.example.locations
//
//import android.app.Application
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//
//class MainViewModelFactory (
//    private val application: Application,
//    private val azimuthViewModel: AzimuthViewModel,
//    private val locationsViewModel: LocationsViewModel
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return MainViewModel(application, azimuthViewModel, locationsViewModel) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}