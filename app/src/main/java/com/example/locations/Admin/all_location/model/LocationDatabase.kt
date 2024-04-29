package com.example.locations.Admin.all_location.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Defines the database holder and serves as the main access point for the underlying connection to the app's data.
@Database(entities = [LocationData::class], version = 1, exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
   // Abstract method to access LocationDao.
   abstract fun locationDao(): LocationDao

   // Companion object to create the database.
   companion object{
      // Volatile variable to ensure that the value of instance is always up-to-date and the same to all execution threads.
      @Volatile
      private var instance: LocationDatabase?=null

        // Function to get the database instance.
      fun getDataBase(context: Context) = instance ?: synchronized(this){
         Room.databaseBuilder(context.applicationContext, LocationDatabase::class.java , "items_db")
            .allowMainThreadQueries().build()
      }
   }
}