package com.example.arnavigationapp.ar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.arnavigationapp.admin.all_location.AdminViewModel
import com.example.arnavigationapp.R
import com.example.arnavigationapp.databinding.StartArFragmentBinding
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlin.math.cos
import kotlin.math.sin
import kotlin.text.*

/**
 * Fragment for the start AR screen.
 * This fragment is used to display the AR view and the arrow model pointing to the destination.
 * The fragment also displays the distance to the destination and the name of the destination.
 * The fragment uses the ARCore library to render the AR view and the arrow model.
 * The fragment uses the Sceneform library to render the 3D models and text on the screen.
 */

class StartARFragment : Fragment() {

    private val adminViewModel: AdminViewModel by activityViewModels()
    private lateinit var binding: StartArFragmentBinding
    private lateinit var arFragment: ArFragment
    private var auth: FirebaseAuth = Firebase.auth
    private val currentUser = auth.currentUser
    private var isModelPlaced = false
    private var isDestinationReached = false
    private var accuracyText = ""
    private lateinit var arrowNode: Node
    private lateinit var targetLocation: Location
    private lateinit var myLocation: Location
    private lateinit var imgUrl: String
    private lateinit var textViewDistance: TextView
    private lateinit var locationIcon: ImageView
    private var accuracy: Float = 0.0f


    // Inflate the layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StartArFragmentBinding.inflate(inflater, container, false) // Inflate the layout
        binding.navigateAgainBtn.setOnClickListener {
            findNavController().navigate(R.id.action_AR_to_Nav)
        }
        binding.exitBtn.setOnClickListener {
            activity?.finishAffinity() // Exit the app
        }
        return binding.root
    }

    // Load the AR fragment and show the plane discovery controller
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StartArFragmentBinding.bind(view) // Get the binding object
        arFragment =
            childFragmentManager.findFragmentById(R.id.sceneView) as ArFragment // Get the AR fragment
        arFragment.planeDiscoveryController.show() // Show the plane discovery controller
        textViewDistance = TextView(context) // Create a new text view for the distance
        locationIcon = ImageView(context) // Create a new text view for the destination
        arrowNode = Node() // Create a new node for the arrow model

        adminViewModel.chosenItem.observe(viewLifecycleOwner) { location ->
            targetLocation =
                adminViewModel.createLocation(location.location) // Create a Location object from the location
            imgUrl = location.imgUrl // Get the name of the location
        }

        adminViewModel.address.observe(viewLifecycleOwner) { address ->
            val (latitude, longitude, currAccuracy) = extractData(address) // Extract the data from the address
            accuracy = currAccuracy
            myLocation =
                adminViewModel.createLocation("$latitude,$longitude") // Create a Location object from the address
            adminViewModel.updateLocationAndAccuracy(
                "$latitude,$longitude",
                currAccuracy
            )
            accuracyText =
                getString(R.string.accuracy) + ": " + accuracy + " " + getString(R.string.meters_accuracy)
            binding.accuracyText.text = accuracyText
        }

        // Update the accuracy text view if the user is an admin
        if(currentUser?.email == adminViewModel.admin){
            binding.accuracyText.visibility = View.VISIBLE
        }

        // Add an update listener to the AR scene view
        arFragment.arSceneView.scene.addOnUpdateListener {
            if (isModelPlaced) {
                // Check if the current location is within 5 meters of the target location
                if (::myLocation.isInitialized && ::targetLocation.isInitialized) {
                    val distance =
                        myLocation.distanceTo(targetLocation) // Calculate the distance to the target location
                    if (distance < 6.0) {
                        // Remove the arrow model from the scene
                        arrowNode.isEnabled = false // Disable the arrow node
                        arrowNode.renderable = null // Set the renderable of the arrow node to null
                        isModelPlaced = false
                        isDestinationReached = true
                        binding.arrivelCardView.visibility = View.VISIBLE // Show the arrival card
                    }
                    updateUI(distance) // Update the UI with the distance
                    updateArrowNode() // Set the position of the model
                    arFragment.arSceneView.planeRenderer.isVisible =
                        false // Hide the plane renderer
                }
            } else {
                handlePlaneDetection() // Handle the plane detection
            }
        }
    }


    // Handle the plane detection
    private fun handlePlaneDetection() {
        val frame = arFragment.arSceneView.arFrame
        if (frame != null && !isModelPlaced && !isDestinationReached) {
            val planes = frame.getUpdatedTrackables(Plane::class.java)
            for (plane in planes) {
                if (plane.trackingState == TrackingState.TRACKING) {
                    if ((adminViewModel.accuracy.value ?: Float.MAX_VALUE) > 5.0f) {
                        continue // Skip if accuracy is poor
                    }
                    binding.loadingCardView.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.loadingText.visibility = View.GONE
                    binding.arrivelCardView.visibility = View.GONE
                    loadArrowModel(arFragment, arrowNode)
                    isModelPlaced = true
                    updateArrowNode() // Ensure the arrow is correctly positioned and rotated after placement
                    break
                }
            }
        }
    }

    // Update the UI with the distance
    private fun updateUI(distanceMeters: Float) {
        val (distance, unitResId) = if (distanceMeters >= 1000) {
            distanceMeters / 1000 to R.string.km
        } else {
            distanceMeters to R.string.meters
        }

        val textDistance = getString(unitResId, distance)
        textRenderer(requireContext(), textViewDistance, textDistance, Vector3(2f, 1.6f, 0f), 60)
        imageRenderer(requireContext(), locationIcon, imgUrl, Vector3(2f, 2f, 0f), 500, 500)
    }

    // Update the arrow node
    private fun updateArrowNode() {
        if (::myLocation.isInitialized && ::targetLocation.isInitialized && isModelPlaced) {
            val direction = calculateDirectionVector(
                myLocation,
                targetLocation
            ) // Calculate the direction to the target location
            val rotation = Quaternion.lookRotation(
                direction,
                Vector3.up()
            ) // Calculate the rotation of the arrow model
            arrowNode.worldRotation = rotation // Update arrow node rotation
            Log.d("StartARFragment", "Arrow updated with direction: $direction, rotation: $rotation")
        }
    }

    // Set the position of the model
    private fun setModelPosition() {
        arrowNode.localPosition = Vector3(0f, -1f, -2f) // 2 meter in front of the camera
        updateArrowNode()
    }

    // Load the arrow model and attach it to the camera
    private fun loadArrowModel(
        fragment: ArFragment, node: Node
    ) {
        val modelUri = Uri.parse("models/direction_arrow.glb") // URI of the arrow model
        ModelRenderable.builder()
            .setSource(
                fragment.context,
                RenderableSource.builder()
                    .setSource(fragment.context, modelUri, RenderableSource.SourceType.GLB)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .setScale(0.2f) // Scale the model
                    .build()
            )
            .setRegistryId(modelUri) // Set the registry ID of the model
            .build()
            .thenAccept { renderable ->
                arrowNode.apply {
                    this.renderable = renderable // Set the renderable of the node
                    arFragment.arSceneView.scene.addChild(this) // Add the node to the scene
                }
                node.setParent(fragment.arSceneView.scene.camera) // Attach the node to the camera
                setModelPosition() // Set the position of the model
            }
            .exceptionally { throwable ->
                Log.e("StartARFragment", "Error loading model", throwable)
                null
            }
    }

    // Calculate the direction to the target location
    private fun calculateBearing(from: Location, to: Location): Float {
        return from.bearingTo(to) // Calculate the bearing to the target location
    }

    // Calculate the direction to the target location
    private fun calculateDirectionVector(myLocation: Location, targetLocation: Location): Vector3 {
        val bearing = calculateBearing(
            myLocation,
            targetLocation
        ) // Calculate the bearing to the target location

        // Adjust the bearing by 90 degrees to align with the AR coordinate system
        val adjustedBearing = (bearing + 90) % 360
        val adjustedBearingRadians = Math.toRadians(adjustedBearing.toDouble()) // Convert bearing to radians


        val x = cos(adjustedBearingRadians).toFloat() // Calculate the x component of the direction vector
        val z = sin(adjustedBearingRadians).toFloat() // Calculate the z component of the direction vector


        return Vector3(x, 0f, z).normalized() // Normalize the direction vector
    }

    // Render 3D text on the screen
    private fun textRenderer(
        context: Context,
        textView: TextView,
        text: String,
        vector3: Vector3,
        size: Int,
        color: Int = R.color.black
    ) {
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(context, color))
        textView.textSize = size.toFloat()
        textView.typeface =
            Typeface.createFromAsset(context.assets, "fonts/gunplay 3d.otf")// Set the text style
        textView.gravity = Gravity.CENTER // Center the text

        // Create a background with rounded corners and shadow
        val background = GradientDrawable()
        background.shape = GradientDrawable.RECTANGLE
        background.cornerRadius = 16f // Set the corner radius
        textView.background = background
        textView.setPadding(0, 8, 0, 8)

        // Add shadow to the text view
        ViewCompat.setElevation(textView, 10f)
        ViewRenderable.builder()
            .setView(context, textView)
            .build()
            .thenAccept { viewRenderable: ViewRenderable ->
                val textNode = object : Node() {
                    override fun onUpdate(p0: FrameTime?) {
                        this.renderable = viewRenderable // Set the renderable of the text node
                        this.localPosition = vector3 // Set the position of the text node
                        val cameraPosition =
                            arFragment.arSceneView.scene.camera.worldPosition // Get the camera position
                        val nodePosition = worldPosition // Get the node position
                        val direction = Vector3.subtract(
                            cameraPosition,
                            nodePosition
                        ) // Calculate the direction from the camera to the node
                        worldRotation = Quaternion.lookRotation(
                            direction,
                            Vector3.up()
                        ) // Set the rotation of the node
                    }
                }
                textNode.setParent(arrowNode) // Set the parent of the text node
            }
            .exceptionally { throwable: Throwable ->
                Log.e("StartARFragment", "Error creating view renderable", throwable)
                null
            }
    }

    private fun imageRenderer(
        context: Context,
        imageView: ImageView,
        imageUrl: String,
        vector3: Vector3,
        width: Int,
        height: Int
    ) {
        imageView.layoutParams = LinearLayout.LayoutParams(width, height)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Create a background with rounded corners and shadow
        val background = GradientDrawable()
        background.shape = GradientDrawable.RECTANGLE
        background.cornerRadius = 16f // Set the corner radius
        background.setColor(Color.TRANSPARENT) // Transparent background
        imageView.background = background
        imageView.setPadding(0, 8, 0, 8)

        // Add shadow to the image view
        ViewCompat.setElevation(imageView, 10f)

        // Load the image using Glide
        Glide.with(context).load(imageUrl).circleCrop().into(imageView)

        ViewRenderable.builder().setView(context, imageView).build()
            .thenAccept { viewRenderable: ViewRenderable ->
                val imageNode = object : Node() {
                    override fun onUpdate(p0: FrameTime?) {
                        this.renderable = viewRenderable // Set the renderable of the image node
                        this.localPosition = vector3 // Set the position of the image node
                        val cameraPosition =
                            arFragment.arSceneView.scene.camera.worldPosition // Get the camera position
                        val nodePosition = worldPosition // Get the node position
                        // Calculate the direction from the camera to the node
                        val direction = Vector3.subtract(cameraPosition, nodePosition)
                        worldRotation = Quaternion.lookRotation(
                            direction,
                            Vector3.up()
                        ) // Set the rotation of the node
                    }
                }
                imageNode.setParent(arrowNode) // Set the parent of the image node
            }
            .exceptionally { throwable: Throwable ->
                Log.e("StartARFragment", "Error creating view renderable", throwable)
                null
            }
    }

    // Extract the data from the address
    private fun extractData(data: String): Triple<String, String, Float> {
        val splitData = data.split(",")
        val latitude = splitData[0]
        val longitude = splitData[1]
        val accuracy = splitData[2].toFloat()
        return Triple(latitude, longitude, accuracy) // Return the extracted data
    }
}

