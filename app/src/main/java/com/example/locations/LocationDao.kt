package com.example.locations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Defines the Data Access Object (DAO) for the LocationData entity.
@Dao
interface LocationDao {

    // Inserts a LocationData object into the database. If a conflict occurs, the existing row will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(locationData: LocationData)

    // Query to select all LocationData from the database.
    @Query("SELECT * FROM location_data")
    suspend fun getAll(): List<LocationData>
}