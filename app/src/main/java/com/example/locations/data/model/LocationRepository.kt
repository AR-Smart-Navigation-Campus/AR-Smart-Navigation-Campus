package com.example.locations.data.model

import android.app.Application

// Repository class to interact with the database
class LocationRepository(application: Application) {
    private var locationDao:LocationDao? = null

    // Initialize the locationDao
    init {
        val db = LocationDatabase.getDataBase(application.applicationContext)
        locationDao = db.locationDao()
    }

    // Get all locations
    fun getLocations() = locationDao?.getAll()
    // Add a location
    fun addLocation(locationData: LocationData) = locationDao?.addLocation(locationData)
    // Update a location
    fun deleteLocation(locationData: LocationData) = locationDao?.deleteLocation(locationData)
    // Get a location by id
    fun getLocation(id:Int) = locationDao?.getLocation(id)
    // Delete all locations
    fun deleteAll() = locationDao?.deleteAll()
}