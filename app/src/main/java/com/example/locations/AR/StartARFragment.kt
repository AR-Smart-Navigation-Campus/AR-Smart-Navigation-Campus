package com.example.locations.AR

import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

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


    // Inflate the layout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity()) // Get the fused location provider client
        binding = StartArFragmentBinding.inflate(inflater, container, false) // Inflate the layout
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    // Load the AR fragment and show the plane discovery controller
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StartArFragmentBinding.bind(view) // Get the binding object
        arFragment = childFragmentManager.findFragmentById(R.id.sceneView) as ArFragment // Get the AR fragment
        arFragment.planeDiscoveryController.show() // Show the plane discovery controller
        arrowNode = Node() // Create a new node for the arrow model

        adminViewModel.chosenItem.observe(viewLifecycleOwner) { location ->
            targetLocation = createLocation(location.location) // Create a Location object from the location
            locationName = location.name
        }

        adminViewModel.address.observe(viewLifecycleOwner) { address ->
            myLocation = createLocation(address) // Create a Location object from the address
        }
        // Add an update listener to the AR scene view
        arFragment.arSceneView.scene.addOnUpdateListener {
            if (isModelPlaced) {
                // Check if the current location is within 5 meters of the target location
                if (::myLocation.isInitialized && ::targetLocation.isInitialized) {
                    val distance = myLocation.distanceTo(targetLocation)
                    binding.Navigating.text = "Navigating to: ${locationName}" // Set the text of the navigating TextView
                    binding.distance.text = "Distance to ${locationName}: ${distance.toInt()} meters" // Set the text of the distance TextView
                    if (distance <= 5) {
                        // Remove the arrow model from the scene
                        arrowNode.isEnabled = false
                        arrowNode.renderable = null
                        isModelPlaced = false
                        isDestinationReached = true
                        Toast.makeText(requireContext(), "You have reached your destination", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
                adminViewModel.chosenItem.observe(viewLifecycleOwner) { location ->
                    targetLocation = createLocation(location.location) // Create a Location object from the location
                    locationName = location.name
                }

                adminViewModel.address.observe(viewLifecycleOwner) { address ->
                    myLocation = createLocation(address) // Create a Location object from the address
                }
                updateArrowNode() // Update the arrow node
                arFragment.arSceneView.planeRenderer.isVisible = false // Hide the plane renderer
                return@addOnUpdateListener
            }
            val frame = arFragment.arSceneView.arFrame // Get the AR frame
            if (frame != null && !isModelPlaced && !isDestinationReached) {
                val planes = frame.getUpdatedTrackables(Plane::class.java) // Get the updated planes
                for (plane in planes) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        loadArrowModel(arFragment, arrowNode, targetLocation, myLocation) // Load the arrow model
                        break
                    }
                }
            }
        }
    }

    private fun updateArrowNode() {
        if (::myLocation.isInitialized && ::targetLocation.isInitialized) {
            val targetVec = targetLocation.toVector3()
            val myLocationVec = myLocation.toVector3()
            // Calculate direction from curr location to target location
            val direction = Vector3.subtract(targetVec, myLocationVec).normalized()
            // Calculate rotation quaternion to point the arrow towards target location
            val rotation = Quaternion.lookRotation(direction, Vector3.up())
            // Update arrow node rotation
            arrowNode.localRotation = rotation
            arrowNode.worldRotation = rotation
        }
    }

    // Set the position and rotation of the model
    private fun setModelPosition(node: Node, targetLocation:Location, myLocation: Location) {
        //val bearing = myLocation.bearingTo(targetLocation)// Calculate the bearing to the target location
        //val bearingInRad = Math.toRadians(bearing.toDouble())// Convert the bearing to radians
        //val rotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), bearingInRad.toFloat())// Create a quaternion for the rotation

        val targetVec = targetLocation.toVector3()
        val myLocationVec = myLocation.toVector3()
        // Calculate direction from curr location to target location
        val direction = Vector3.subtract(targetVec, myLocationVec).normalized()
        // Calculate rotation quaternion to point the arrow towards target location
        val rotation = Quaternion.lookRotation(direction, Vector3.up())
        // Update arrow node position and rotation
        node.localRotation = rotation // Set the local rotation of the model
        node.localPosition = Vector3(0f, -1f, -2f) // 2 meter in front of the camera
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
                    .setScale(0.2f)
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
                setModelPosition(node, locationData, address) // Set the position of the model
                isModelPlaced = true // Set the flag to true
            }
            .exceptionally { throwable ->
                Log.e("StartARFragment", "Error loading model", throwable)
                null
            }
    }


    // Create a Location object from a string
    private fun createLocation(location:String): Location {
        val coordinates = location.split(",")
        Location("provider").apply {
            latitude = coordinates[0].toDouble()
            longitude = coordinates[1].toDouble()
            return this
        }
    }

    // Convert a Location object to a Vector3 object
    private fun Location.toVector3(): Vector3 {
        return Vector3(this.latitude.toFloat(), 0f, this.longitude.toFloat())
    }

}
