package com.example.locations

import androidx.room.Database
import androidx.room.RoomDatabase

// Defines the database holder and serves as the main access point for the underlying connection to the app's data.
@Database(entities = [LocationData::class], version = 1, exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
   // Abstract method to access LocationDao.
   abstract fun locationDao(): LocationDao
}