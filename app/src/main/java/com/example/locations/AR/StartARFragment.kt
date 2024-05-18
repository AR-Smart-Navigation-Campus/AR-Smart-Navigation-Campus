package com.example.locations.AR

import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.locations.Admin.all_location.AdminViewModel
import com.example.locations.R
import com.example.locations.databinding.StartArFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StartARFragment : Fragment() {

    private val adminViewModel: AdminViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: StartArFragmentBinding
    private lateinit var arFragment: ArFragment
    private var modelRenderable: ModelRenderable? = null
    private lateinit var modelNode: TransformableNode
    private lateinit var sensorManager: SensorManager
    private lateinit var orientationSensor: Sensor
    private var rotationAngle: Float = 0f

    private val onUpdateListener: (FrameTime) -> Unit = { onUpdateFrame() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding = StartArFragmentBinding.inflate(inflater, container, false)
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StartArFragmentBinding.bind(view)
        arFragment = childFragmentManager.findFragmentById(R.id.sceneView) as ArFragment
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(onUpdateListener)
        loadModel(Uri.parse("file:///android_asset/models/direction_arrow.glb"))
    }

    private fun onUpdateFrame() {
        val frame = arFragment.arSceneView.arFrame ?: return
        if (frame.camera.trackingState != TrackingState.TRACKING) {
            Log.d("ARDebug", "Tracking not yet established")
            return
        }

        val cameraPose = frame.camera.displayOrientedPose
        val position = Vector3(cameraPose.tx(), cameraPose.ty()-0.4f, cameraPose.tz() - 1.0f)
        val rotation = floatArrayToQuaternion(cameraPose.rotationQuaternion)
        var targetLocation: Location? = null
        var myLocation: Location? = null

//        adminViewModel.chosenItem.observe(viewLifecycleOwner) { location ->
//            targetLocation = createLocation(location.location)
//        }
//
//        adminViewModel.address.observe(viewLifecycleOwner) { address ->
//            myLocation = createLocation(address)
//        }
        if (::modelNode.isInitialized) {
            modelNode.apply {
                // Calculate the bearing to the target location
                //val bearing = myLocation!!.bearingTo(targetLocation!!)

               // Convert the bearing to radians
                //val bearingInRad = Math.toRadians(bearing.toDouble())

                // Create a quaternion for the rotation
                //val rotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), (-bearingInRad).toFloat())

                // Set the local rotation of the model
                //localRotation = rotation

                worldPosition = position
                worldRotation = rotation
                localRotation = Quaternion.axisAngle(Vector3(1f, 1f ,1f), 90f)
                renderable = modelRenderable // Ensure the renderable is set
                if (!isSelected) {
                    select()
                }
            }
            Log.d("ARDebug", "Model position updated: $position, rotation updated: $rotation")
        }
    }
//    private fun onUpdateFrame() {
//        val frame = arFragment.arSceneView.arFrame ?: return
//        if (frame.camera.trackingState != TrackingState.TRACKING) {
//            Log.d("ARDebug", "Tracking not yet established")
//            return
//        }
//
//        val cameraPose = frame.camera.displayOrientedPose
//        val position = Vector3(cameraPose.tx(), cameraPose.ty(), cameraPose.tz() - 1.0f)
//        val rotation = floatArrayToQuaternion(cameraPose.rotationQuaternion)
//
//        var targetLocation: Location? = null
//        var myLocation: Location? = null
//
//        adminViewModel.chosenItem.observe(viewLifecycleOwner) { location ->
//            targetLocation = createLocation(location.location)
//        }
//
//        adminViewModel.address.observe(viewLifecycleOwner) { address ->
//            myLocation = createLocation(address)
//        }
//
//        if (::modelNode.isInitialized && targetLocation != null && myLocation != null) {
//            modelNode.apply {
//                worldPosition = position
//                worldRotation = rotation
//
//                // Calculate the bearing to the target location
//                val bearing = myLocation!!.bearingTo(targetLocation!!)
//
//                // Convert the bearing to radians
//                val bearingInRad = Math.toRadians(bearing.toDouble())
//
//                // Create a quaternion for the rotation
//                val rotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), (-bearingInRad).toFloat())
//
//                // Set the local rotation of the model
//                localRotation = rotation
//
//                renderable = modelRenderable // Ensure the renderable is set
//                if (!isSelected) {
//                    select()
//                }
//            }
//            Log.d("ARDebug", "Model position updated: $position, rotation updated: $rotation")
//        }
//    }
    private fun loadModel(modelUri: Uri) {
        val renderableSource = RenderableSource.builder()
            .setSource(arFragment.context, modelUri, RenderableSource.SourceType.GLB)
            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
            .build()

        ModelRenderable.builder()
            .setSource(arFragment.context, renderableSource)
            .build()
            .thenAccept { renderable ->
                Log.d("ARDebug", "Model loaded successfully")
                modelRenderable = renderable
                addNodeToScene()
            }
            .exceptionally { throwable ->
                val activity = arFragment.activity
                Log.e("ARDebug", "Error loading model: ${throwable.message}")
                if (activity != null) {
                    AlertDialog.Builder(activity)
                        .setTitle("Error")
                        .setMessage(throwable.message)
                        .setCancelable(true)
                        .create()
                        .show()
                }
                null
            }
    }

    private fun addNodeToScene() {
        modelRenderable?.let { renderable ->
            val anchorNode = AnchorNode().apply {
                localScale = Vector3(0.2f, 0.2f, 0.2f)
                localRotation=Quaternion.axisAngle(Vector3(1f, 0f, 0f), 0f)
                arFragment.arSceneView.scene.addChild(this)
            }

            modelNode = TransformableNode(arFragment.transformationSystem).apply {
                this.renderable = renderable
                setParent(anchorNode)
                select()
            }
            Log.d("ARDebug", "Model added to scene with scale: ${anchorNode.localScale}")
        }
    }

    private fun floatArrayToQuaternion(floatArray: FloatArray): Quaternion {
        return Quaternion(floatArray[0], floatArray[1], floatArray[2], floatArray[3])
    }
}
private fun createLocation(location:String): Location {
    val coordinates = location.split(",")
    Location("provider").apply {
        latitude = coordinates[0].toDouble()
        longitude = coordinates[1].toDouble()
        return this
    }
}


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding = StartArFragmentBinding.bind(view)
//        arFragment = childFragmentManager.findFragmentById(R.id.sceneView) as ArFragment
//        arFragment.planeDiscoveryController.show()
//
//        arFragment.arSceneView.scene.addOnUpdateListener {
//            if (isModelPlaced) {
//                arFragment.arSceneView.planeRenderer.isVisible = false
//                return@addOnUpdateListener
//            }
//            val frame = arFragment.arSceneView.arFrame
//            if (frame != null) {
//                val planes = frame.getUpdatedTrackables(Plane::class.java)
//                for (plane in planes) {
//                    if (plane.trackingState == TrackingState.TRACKING) {
//                        val pose = Pose.makeTranslation(0f, -1.5f, -2f) // Change this value based on where you want to place the object
//                        val anchor = plane.createAnchor(pose)
//                        val modelUri = Uri.parse("models/direction_arrow.glb")
//                        var targetLocation = Location("provider")
//                        var myLocation = Location("provider")
//                        val node = Node()
//
//
//                        adminViewModel.chosenItem.observe(viewLifecycleOwner) { location->
//                            targetLocation  = createLocation(location.location)
//                        }
//
//
//                        adminViewModel.address.observe(viewLifecycleOwner) { address ->
//                            myLocation = createLocation(address)
//                        }
//
//                        placeObject(arFragment, anchor, modelUri, node,targetLocation, myLocation)
//                        break
//                    }
//                }
//            }
//        }
//    }

//    private fun setModelPosition(node: Node, targetLocation:Location, myLocation: Location) {
//
//        // Calculate the bearing to the target location
//        val bearing = myLocation.bearingTo(targetLocation)
//        // Convert the bearing to radians
//        val bearingInRad = Math.toRadians(bearing.toDouble()) * 10
//        // Create a quaternion for the rotation
//        val rotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), (-bearingInRad).toFloat())
//
//        // Set the local rotation of the model
//        node.localRotation = rotation
//
//        // Set the world position of the model to the world position of the camera, but with a fixed y-coordinate
//        node.worldPosition = node.parent?.worldPosition?.let { Vector3(it.x, -2f, node.parent?.worldPosition!!.z) }
//    }
//
//    private fun placeObject(
//        fragment: ArFragment,
//        anchor: Anchor,
//        modelUri: Uri,
//        node: Node,
//        locationData: Location,
//        address: Location
//    ) {
//        ModelRenderable.builder()
//            .setSource(
//                fragment.context,
//                RenderableSource.builder()
//                    .setSource(fragment.context, modelUri, RenderableSource.SourceType.GLB)
//                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                    .setScale(0.3f)
//                    .build()
//            )
//            .setRegistryId(modelUri)
//            .build()
//            .thenAccept { renderable ->
//                Log.d("StartARFragment", "Model loaded successfully")
//                node.setParent(fragment.arSceneView.scene.camera)
//                setModelPosition(node, locationData, address)
//                addNodeToScene(fragment, anchor, renderable , node)
//            }
//            .exceptionally { throwable ->
//                Log.e("StartARFragment", "Error loading model", throwable)
//                null
//            }
//    }
//
//    private fun addNodeToScene(
//        fragment: ArFragment,
//        anchor: Anchor,
//        renderable: Renderable,
//        node: Node
//    ) {
//        val anchorNode = AnchorNode(anchor)
//        node.setParent(anchorNode)
//        node.renderable = renderable
//        fragment.arSceneView.scene.addChild(node)
//        isModelPlaced = true
//    }


