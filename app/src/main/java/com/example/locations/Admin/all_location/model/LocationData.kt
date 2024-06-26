package com.example.locations.Admin.all_location.model

import android.net.Uri

// used for efficiently passing data between different Android components, such as activities, fragments, and services

// Defines the LocationData entity, represents a table within the database.

data class LocationData(

    val id:Long=0,
    val name: String ="",
    val location: String="",
    val azimuth: String="",
    val imgUrl: String="",
)

