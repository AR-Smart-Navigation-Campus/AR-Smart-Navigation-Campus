package com.example.arnavigationapp.admin.all_location.model

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
                val locations =
                    snapshot.toObjects(LocationData::class.java) // Convert snapshot to list of LocationData
                callback(Result.success(locations)) // Return list of locations
            } else {
                Log.d("Firestore", "No locations found")
                callback(Result.success(emptyList())) // Return empty list if no locations found
            }
        }
    }

    // Saves a building to Firestore
    fun saveBuildingToFirestore(building: LocationData) {
        // Create a new building data object
        val buildingData = hashMapOf(
            "id" to building.id,
            "name" to building.name,
            "location" to building.location,
            "azimuth" to building.azimuth,
            "description" to building.description,
            "imgUrl" to building.imgUrl
        )

        // Add the building data to Firestore
        db.collection("buildings")
            .add(buildingData)
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    // Deletes a building from Firestore
    fun deleteBuildingFromFirestore(id: Long) {
        db.collection("buildings").get().addOnSuccessListener { result ->
            for (document in result) {
                if (document.data["id"] == id) {
                    val name = document.data["name"]
                    // If building is found, check for related buttons and delete them
                    db.collection("buttons").get().addOnSuccessListener { btnResult ->
                        for (btnDocument in btnResult) {
                            if (btnDocument.data["text"] == name) {
                                deleteButtonFromFirestore(btnDocument.id) // Delete button if it matches the building's name
                            }
                        }
                    }
                    db.collection("buildings").document(document.id)
                        .delete() // Delete the building document
                }
            }
        }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting document", e)
            }
    }

    // Uploads an image to Firebase Storage and gets the URL
    fun uploadImageToFirebaseStorage(imageUri: Uri, callback: (String) -> Unit) {
        // Generate a unique file name for the image
        val storageReference =
            FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString()) // Return the download URL of the uploaded image
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting download URL: ${exception.message}", exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "Firebase",
                    "Error uploading image to Firebase Storage: ${exception.message}",
                    exception
                )
            }
    }

    // Save button state to Firestore
    fun saveButtonStateToFirestore(x: Float, y: Float, text: String) {
        // Create a new button data object
        val buttonData = hashMapOf(
            "x" to x,
            "y" to y,
            "text" to text
        )

        // Add the button data to Firestore
        db.collection("buttons")
            .add(buttonData)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    // Load button state from Firestore with callback
    fun loadButtonStateFromFirestore(callback: (List<ButtonData>) -> Unit) {
        db.collection("buttons")
            .get()
            .addOnSuccessListener { result ->
                val buttonList =
                    mutableListOf<ButtonData>() // List to store the retrieved button data
                for (document in result) {
                    val x = document.getDouble("x")?.toFloat() ?: -1f
                    val y = document.getDouble("y")?.toFloat() ?: -1f
                    val text = document.getString("text") ?: ""

                    if (x != -1f && y != -1f) {
                        buttonList.add(
                            ButtonData(
                                x,
                                y,
                                text
                            )
                        ) // Add the button data to the list if valid
                    }
                }
                callback(buttonList) // Return the list of buttons through the callback
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
            }
    }

    // Deletes a button from Firestore
    private fun deleteButtonFromFirestore(id: String) {
        db.collection("buttons").get().addOnSuccessListener { result ->
            for (document in result) {
                if (document.id == id) {
                    db.collection("buttons").document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error deleting document", e)
                        }
                }
            }
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Error getting documents", e)
        }
    }

    // Method to update the button's position in Firestore
    fun updateButtonPositionInFirestore(buttonText: String, newX: Float, newY: Float) {
        db.collection("buttons")
            .whereEqualTo("text", buttonText) // Find the button document by its text
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("buttons").document(document.id)
                        .update(mapOf("x" to newX, "y" to newY)) // Update the x and y coordinates
                        .addOnSuccessListener {
                            Log.d("Firestore", "Button position updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error updating button position", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error finding button document", e)
            }
    }

    // Data class to represent button data
    data class ButtonData(val x: Float, val y: Float, val text: String)
}
