package com.example.locations.AR

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.locations.R
import com.example.locations.databinding.StartArFragmentBinding
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class StartARFragment : Fragment() {

    private lateinit var binding: StartArFragmentBinding
    private lateinit var arFragment: ArFragment
    private var isModelPlaced = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        arFragment.arSceneView.scene.addOnUpdateListener {
            if (isModelPlaced) {
                arFragment.arSceneView.planeRenderer.isVisible = false
                return@addOnUpdateListener
            }
            val frame = arFragment.arSceneView.arFrame
            if (frame != null) {
                val planes = frame.getUpdatedTrackables(Plane::class.java)
                for (plane in planes) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        val pose = Pose.makeTranslation(0f, -1.5f, -2f) // Change this value based on where you want to place the object
                        val anchor = plane.createAnchor(pose)
                        val modelUri = Uri.parse("models/direction_arrow.glb")
                        placeObject(arFragment, anchor, modelUri)
                        break
                    }
                }
            }
        }
    }
    private fun placeObject(fragment: ArFragment, anchor: Anchor, modelUri: Uri) {
        ModelRenderable.builder()
            .setSource(
                fragment.context,
                RenderableSource.builder()
                    .setSource(fragment.context, modelUri, RenderableSource.SourceType.GLB)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT).setScale(0.3f)
                    .build()
            )
            .setRegistryId(modelUri)
            .build()
            .thenAccept { renderable ->
                addNodeToScene(fragment, anchor, renderable)
            }
    }
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = Node()
        node.setParent(anchorNode)
        node.renderable = renderable
        fragment.arSceneView.scene.addChild(anchorNode)
        isModelPlaced = true
    }
}