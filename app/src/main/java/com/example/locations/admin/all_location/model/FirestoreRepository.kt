package com.example.locations.admin.all_location.model

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

/**
 * FirestoreRepository class that provides functions to interact with Firestore.
 * Fetches the locations from Firestore and updates the locationData LiveData object.
 * Provides functions to add and delete entries from Firestore.
 * Provides functions to upload an image to Firebase Storage and get the URL.
 */
class FirestoreRepository {

    private val db = Firebase.firestore // Firestore instance

    // Fetches the locations from Firestore
    fun fetchLocations(callback: (Result<List<LocationData>>) -> Unit) {
        db.collection("buildings").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.d("Firestore", "Error getting documents: ", error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val locations = snapshot.toObjects(LocationData::class.java)
                callback(Result.success(locations)) // Return list of locations
            } else {
                Log.d("Firestore", "No locations found")
                callback(Result.success(emptyList())) // Return empty list if no locations found
            }

//            if (snapshot != null && !snapshot.isEmpty) {
//                val locations = snapshot.toObjects(LocationData::class.java)
//                locationData.postValue(locations)
//            } else {
//                Log.d("Firestore", "No locations found")
//            }
        }
    }

    // Saves a building to Firestore
    fun saveBuildingToFirestore(building: LocationData) {
        val buildingData = hashMapOf(
            "id" to building.id,
            "name" to building.name,
            "location" to building.location,
            "azimuth" to building.azimuth,
            "description" to building.description,
            "imgUrl" to building.imgUrl
        )

        db.collection("buildings")
            .add(buildingData)
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    // Deletes a building from Firestore
    fun deleteBuildingFromFirestore(id:Long) {
        db.collection("buildings").get().addOnSuccessListener { result ->
            for (document in result) {
                if (document.data["id"] == id) {
                    db.collection("buildings").document(document.id).delete()
                }
            }
        }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting document", e)
            }
    }

    // Uploads an image to Firebase Storage and gets the URL
    fun uploadImageToFirebaseStorage(imageUri: Uri, callback: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting download URL: ${exception.message}", exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error uploading image to Firebase Storage: ${exception.message}", exception)
            }
    }
}