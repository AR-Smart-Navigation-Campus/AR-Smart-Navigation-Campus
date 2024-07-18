package com.example.arnavigationapp.ar

import android.content.Context
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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StartArFragmentBinding.inflate(inflater, container, false) // Inflate the layout
        binding.navigateAgainBtn.setOnClickListener {
            findNavController().navigate(R.id.action_AR_to_StartNav)
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
        textViewDestination = TextView(context) // Create a new text view for the destination
        arrowNode = Node() // Create a new node for the arrow model

        adminViewModel.chosenItem.observe(viewLifecycleOwner) { location ->
            targetLocation =
                adminViewModel.createLocation(location.location) // Create a Location object from the location
            locationName = location.name // Get the name of the location
        }

        adminViewModel.address.observe(viewLifecycleOwner) { address ->
            val (latitude, longitude, currAccuracy) = extractData(address) // Extract the data from the address
            accuracy = currAccuracy
            myLocation =
                adminViewModel.createLocation("$latitude,$longitude") // Create a Location object from the address
        }

        // Add an update listener to the AR scene view
        arFragment.arSceneView.scene.addOnUpdateListener {
            if (isModelPlaced) {
                // Check if the current location is within 5 meters of the target location
                if (::myLocation.isInitialized && ::targetLocation.isInitialized) {
                    var distance = myLocation.distanceTo(targetLocation) // Calculate the distance to the target location
                    val textDistance: String
                    if (distance.toInt() >= 1000) {
                        distance /= 1000
                        textDistance = getString(R.string.km, distance)
                    } else {
                        textDistance = "" + distance.toInt() + "" + getString(R.string.meters_accuracy)
                    }
                    val textID =
                        adminViewModel.getLocationNameResId(locationName) // Get the text resource ID
                    val text = getString(textID) // Get the text from the resource ID
                    textRenderer(requireContext(), textViewDistance, textDistance, Vector3(2f, 1.6f, 0f), 60) // Render the text on the screen
                    textRenderer(requireContext(), textViewDestination, text, Vector3(2f, 2f, 0f), 70) // Render the text on the screen

                    if (distance < 6.0) {
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
                        myLocation = adminViewModel.createLocation("$latitude,$longitude") // Create a Location object from the address
                    }
                    setModelPosition() // Set the position of the model
                    arFragment.arSceneView.planeRenderer.isVisible = false // Hide the plane renderer

                    return@addOnUpdateListener // Return from the listener
                }
            }
            val frame = arFragment.arSceneView.arFrame // Get the AR frame
            if (frame != null && !isModelPlaced && !isDestinationReached) {
                val planes = frame.getUpdatedTrackables(Plane::class.java) // Get the updated planes
                for (plane in planes) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        if (accuracy > 5.0) {
                            continue // Skip the plane if the accuracy is greater than 5 meters
                        }
                        binding.progressBar.visibility = View.GONE
                        binding.loadingText.visibility = View.GONE
                        loadArrowModel(arFragment, arrowNode) // Load the arrow model
                        isModelPlaced = true // Set the flag to true
                        break
                    }
                }
            }
        }
    }

    // Update the arrow node
    private fun updateArrowNode() {
        if (::myLocation.isInitialized && ::targetLocation.isInitialized) {
            val direction = calculateDirectionVector(
                myLocation,
                targetLocation
            ) // Calculate the direction to the target location
            val rotation = Quaternion.lookRotation(
                direction,
                Vector3.up()
            ) // Calculate the rotation of the arrow model
            arrowNode.worldRotation = rotation // Update arrow node rotation
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
        val bearing = from.bearingTo(to) // Calculate the bearing to the target location
        return (-bearing + 360) % 360 // Normalize the bearing
    }

    // Calculate the direction to the target location
    private fun calculateDirectionVector(myLocation: Location, targetLocation: Location): Vector3 {
        val bearing = calculateBearing(
            myLocation,
            targetLocation
        ) // Calculate the bearing to the target location
        val bearingRadians = Math.toRadians(bearing.toDouble()) // Convert the bearing to radians

        val x = cos(bearingRadians).toFloat() // Calculate the x component of the direction vector
        val z = sin(bearingRadians).toFloat() // Calculate the z component of the direction vector

        return Vector3(x, 0f, z).normalized() // Normalize the direction vector
    }

    // Render 3D text on the screen
    private fun textRenderer(context: Context, textView: TextView, text: String, vector3: Vector3, size: Int, color: Int = R.color.black
    ) {
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(context, color))
        textView.textSize = size.toFloat()
        textView.typeface = Typeface.createFromAsset(context.assets, "fonts/gunplay 3d.otf")// Set the text style
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
                        val cameraPosition = arFragment.arSceneView.scene.camera.worldPosition // Get the camera position
                        val nodePosition = worldPosition // Get the node position
                        val direction = Vector3.subtract(cameraPosition, nodePosition) // Calculate the direction from the camera to the node
                        worldRotation = Quaternion.lookRotation(direction, Vector3.up()) // Set the rotation of the node
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
