package com.example.locations.Admin.all_location.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize // used for efficiently passing data between different Android components, such as activities, fragments, and services

// Defines the LocationData entity, represents a table within the database.
@Entity(tableName = "location_data")
data class LocationData(

    // Primary key for the entity, auto generated by Room.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Represents the location data.
    @ColumnInfo(name = "location")
    val location: String,


    // Represents the name of the location.
    @ColumnInfo(name = "content")
    val text: String  ,

    // Represents the azimuth data.
    @ColumnInfo(name = "azimuth")
    val azimuth: String ,

    // Represents the image data.
    @ColumnInfo(name = "image")
    val img: String

): Parcelable
