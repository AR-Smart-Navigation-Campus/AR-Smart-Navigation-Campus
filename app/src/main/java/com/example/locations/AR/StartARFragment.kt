package com.example.locations.AR

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.locations.Admin.all_location.AdminViewModel
import com.example.locations.R
import com.example.locations.databinding.StartArFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fragment class for the AR view.
 */
class StartARFragment : Fragment() {

    private val adminViewModel:  AdminViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding : StartArFragmentBinding
    private lateinit var arFragment: ArFragment
    private var isModelPlaced = false
    private var isDestinationReached = false
    private lateinit var arrowNode: Node
    private lateinit var targetLocation: Location
    private lateinit var myLocation: Location
    private lateinit var locationName: String
    private lateinit var textViewDistance: TextView
    private lateinit var textViewDestination: TextView
    private var accuracy: Float = 0.0f


    // Inflate the layout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity()) // Get the fused location provider client
        binding = StartArFragmentBinding.inflate(inflater, container, false) // Inflate the layout
        binding.navigateAgainBtn.setOnClickListener {
            findNavController().navigate(R.id.action_AR_to_StartNav)
        }
        binding.exitBtn.setOnClickListener {
            activity?.finishAffinity()
        }
        return binding.root
    }

    // Load the AR fragment and show the plane discovery controller
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StartArFragmentBinding.bind(view) // Get the binding object
        arFragment = childFragmentManager.findFragmentById(R.id.sceneView) as ArFragment // Get the AR fragment
        arFragment.planeDiscoveryController.show() // Show the plane discovery controller
        textViewDistance = TextView(context) // Create a new text view for the distance
        textViewDestination = TextView(context) // Create a new text view for the destination
        arrowNode = Node() // Create a new node for the arrow model

        adminViewModel.chosenItem.observe(viewLifecycleOwner) { location ->
            targetLocation = createLocation(location.location) // Create a Location object from the location
            locationName = location.name // Get the name of the location
        }

        adminViewModel.address.observe(viewLifecycleOwner) { address ->
            val (latitude, longitude, currAccuracy) = extractData(address) // Extract the data from the address
            accuracy = currAccuracy
            myLocation = createLocation("$latitude,$longitude") // Create a Location object from the address
        }
        // Add an update listener to the AR scene view
        arFragment.arSceneView.scene.addOnUpdateListener {
            if (isModelPlaced) {
                // Check if the current location is within 5 meters of the target location
                if (::myLocation.isInitialized && ::targetLocation.isInitialized) {
                    val distance = myLocation.distanceTo(targetLocation) // Calculate the distance to the target location
                    binding.Navigating.text = "Navigating to: ${targetLocation.latitude} , ${targetLocation.longitude}" // Set the text of the navigating TextView
                    binding.myLocation.text = "current: ${myLocation.latitude} , ${myLocation.longitude}"

                    val textDistance = "${distance.toInt()} meters"
                    val text = locationName
                    textRenderer(textViewDistance,textDistance, Vector3(2f, 1.9f, 0f) , 22) // Render the text on the screen
                    textRenderer(textViewDestination,text, Vector3(2f, 2f, 0f) , 35) // Render the text on the screen

                    if (distance < 6) {
                        // Remove the arrow model from the scene
                        arrowNode.isEnabled = false // Disable the arrow node
                        arrowNode.renderable = null // Set the renderable of the arrow node to null
                        isModelPlaced = false
                        isDestinationReached = true
                        binding.arrivelCardView.visibility = View.VISIBLE // Show the arrival card
                    }
                    adminViewModel.address.observe(viewLifecycleOwner) { address ->
                        val (latitude, longitude, currAccuracy) = extractData(address) // Extract the data from the address
                        accuracy = currAccuracy
                        myLocation = createLocation("$latitude,$longitude") // Create a Location object from the address
                    }

                    updateArrowNode() // Update the arrow node
                    arFragment.arSceneView.planeRenderer.isVisible = false // Hide the plane renderer

                    return@addOnUpdateListener // Return from the listener
                }
            }
            val frame = arFragment.arSceneView.arFrame // Get the AR frame
            if (frame != null && !isModelPlaced && !isDestinationReached) {
                val planes = frame.getUpdatedTrackables(Plane::class.java) // Get the updated planes
                for (plane in planes) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        binding.Navigating.text = "Navigating to: ${locationName}"
                        binding.myLocation.text = "accuracy: $accuracy"
                        if (accuracy>5.0){
                            continue // Skip the plane if the accuracy is greater than 5 meters
                        }
                        binding.progressBar.visibility = View.GONE
                        binding.loadingText.visibility = View.GONE
                        loadArrowModel(arFragment, arrowNode, targetLocation, myLocation) // Load the arrow model
                        break
                    }
                }
            }
        }
    }

    // Update the arrow node
    private fun updateArrowNode() {
        if (::myLocation.isInitialized && ::targetLocation.isInitialized) {
            val direction = calculateDirectionVector(myLocation, targetLocation) // Calculate the direction to the target location
            val rotation = Quaternion.lookRotation(direction, Vector3.up()) // Calculate the rotation of the arrow model

            Log.d("ARFragment", "myLocation: ${myLocation.latitude}, ${myLocation.longitude}")
            Log.d("ARFragment", "targetLocation: ${targetLocation.latitude}, ${targetLocation.longitude}")
            Log.d("ARFragment", "Direction: $direction, Rotation: $rotation")


            arrowNode.worldRotation = rotation // Update arrow node rotation
        }
    }

    // Set the position of the model
    private fun setModelPosition() {
        arrowNode.localPosition = Vector3(0f, -1f, -2f) // 2 meter in front of the camera
        Log.d("ARFragment", "setModelPosition - Position: ${arrowNode.localPosition}")
        updateArrowNode()

    }

    // Load the arrow model and attach it to the camera
    private fun loadArrowModel(fragment: ArFragment, node: Node, locationData: Location, address: Location
    ) {
                val modelUri = Uri.parse("models/direction_arrow.glb")
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
                        isModelPlaced = true // Set the flag to true
                    }
                    .exceptionally { throwable ->
                        Log.e("StartARFragment", "Error loading model", throwable)
                        null
                    }
            }

    // Create a Location object from a string
    private fun createLocation(location:String): Location {
        val coordinates = location.split(",") // Split the string by comma
        Location("provider").apply {
            latitude = coordinates[0].toDouble() // Set the latitude
            longitude = coordinates[1].toDouble() // Set the longitude
            return this
        }
    }

    // Calculate the direction to the target location
    private fun calculateBearing(from: Location, to: Location): Float {
        val bearing = from.bearingTo(to) // Calculate the bearing to the target location
        return (bearing + 360) % 360 // Normalize the bearing
    }

    // Calculate the direction to the target location
    private fun calculateDirectionVector(myLocation: Location, targetLocation: Location): Vector3 {
        val bearing = calculateBearing(myLocation, targetLocation) // Calculate the bearing to the target location
        val bearingRadians = Math.toRadians(bearing.toDouble()) // Convert the bearing to radians

        val x = cos(bearingRadians).toFloat() // Calculate the x component of the direction vector
        val z = sin(bearingRadians).toFloat() // Calculate the z component of the direction vector

        return Vector3(x, 0f, z).normalized()
    }

    // Render text on the screen
    private fun textRenderer(textView: TextView, text:String, vector3: Vector3, size:Int , color:Int = R.color.white) {
        textView.text = text
        textView.setTextColor(getColor(resources, color, null))
        textView.textSize = size.toFloat()
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

    // Extract the data from the address
    private fun extractData(data: String): Triple<String, String, Float> {
        val splitData = data.split(",")
        val latitude = splitData[0]
        val longitude = splitData[1]
        val accuracy = splitData[2].toFloat()
        return Triple(latitude, longitude, accuracy) // Return the extracted data
    }
}
