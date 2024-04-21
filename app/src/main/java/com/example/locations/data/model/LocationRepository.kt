package com.example.locations.data.model

import android.app.Application

class LocationRepository(application: Application) {
    private var locationDao:LocationDao? = null

    init {
        val db = LocationDatabase.getDataBase(application.applicationContext)
        locationDao = db.locationDao()
    }

    fun getLocations() = locationDao?.getAll()

    fun addLocation(locationData: LocationData) = locationDao?.addLocation(locationData)

    fun deleteLocation(locationData: LocationData) = locationDao?.deleteLocation(locationData)

    fun getLocation(id:Int) = locationDao?.getLocation(id)

    fun deleteAll() = locationDao?.deleteAll()
}