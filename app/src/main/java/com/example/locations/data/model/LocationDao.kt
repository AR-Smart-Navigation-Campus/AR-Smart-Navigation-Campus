package com.example.locations.data.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// Defines the Data Access Object (DAO) for the LocationData entity.
@Dao
interface LocationDao {

    // Inserts a LocationData object into the database. If a conflict occurs, the existing row will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocation(locationData: LocationData)

    @Update
    fun updateLocation(locationData: LocationData)
    // Query to select all LocationData from the database.
    @Query("SELECT * FROM location_data")
    fun getAll(): LiveData<List<LocationData>>
    @Query("SELECT * FROM location_data WHERE id LIKE :id")
    fun getLocation(id:Int):LocationData

    // Query to delete a LocationData object from the database.
    @Delete
    fun deleteLocation(locationData: LocationData)
    @Query("DELETE FROM location_data")
    fun deleteAll()
}