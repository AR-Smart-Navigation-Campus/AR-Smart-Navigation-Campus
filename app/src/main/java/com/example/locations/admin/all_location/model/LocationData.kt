package com.example.locations.admin.all_location.model

/**
 * used for efficiently passing data between different Android components
 * such as activities, fragments, and services
 * It is a simple data class that is used to store location data.
 * Defines the LocationData entity, represents a table within the database.
 */
data class LocationData(

    val id:Long=0,
    val name: String ="",
    val location: String="",
    val azimuth: String="",
    val description: String="",
    val imgUrl: String="",
)

