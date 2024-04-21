package com.example.locations.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Defines the database holder and serves as the main access point for the underlying connection to the app's data.
@Database(entities = [LocationData::class], version = 1, exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
   // Abstract method to access LocationDao.
   abstract fun locationDao(): LocationDao

   companion object{
      @Volatile
      private var instance:LocationDatabase?=null

      fun getDataBase(context: Context) = instance ?: synchronized(this){
         Room.databaseBuilder(context.applicationContext, LocationDatabase::class.java , "items_db")
            .allowMainThreadQueries().build()
      }
   }
}