package com.gerardbradshaw.mixup.ui.editor

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.mixup.BaseApplication
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.collageview.AbstractCollageView
import com.gerardbradshaw.mixup.collageview.CollageViewFactory
import com.ortiz.touchview.TouchImageView

class EditorFragment : Fragment(), View.OnClickListener {

  //@Inject lateinit var glideInstance: RequestManager

  private lateinit var rootView: View
  private lateinit var viewModel: EditorViewModel
  private lateinit var collageViewFactory: CollageViewFactory

  private lateinit var collageFrameParent: FrameLayout
  private lateinit var collageFrame: FrameLayout
  private lateinit var collage: AbstractCollageView

  private var lastImageClickedIndex: Int = -1


  // ------------------------ INITIALIZATION ------------------------

  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {

    (requireActivity().application as BaseApplication).getAppComponent()
      .editorComponent().create().inject(this)

    return inflater.inflate(R.layout.fragment_editor, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    viewModel = ViewModelProvider(this).get(EditorViewModel::class.java)
    rootView = view

    if (savedInstanceState == null || !savedInstanceState.getBoolean(IS_RETURN_SESSION)) {
      // TODO restore saved data
    }

    viewModel.canvasRatio.observe(requireActivity(), Observer { onRatioChange(it) })

    initOptionsButtons()
    showCollageTypesInRecycler()
    initCollage()
  }

  private fun initCollage() {
    collageFrameParent = rootView.findViewById(R.id.parent_frame)

    collageFrame = rootView.findViewById(R.id.collage_frame)

    collageFrame.viewTreeObserver.addOnGlobalLayoutListener(
      object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
          if (collageFrame.height > 0) {
            initCollageViewFactory()
            initDefaultCollage()

            collageFrame.viewTreeObserver.removeOnGlobalLayoutListener(this)
          }
        }
      })
  }

  private fun initDefaultCollage() {
    collage = collageViewFactory.getCollage(CollageViewFactory.CollageType.THREE_IMAGE_2)

    collageFrame.addView(collage)

    collage.setImageClickListener(this)
  }

  private fun initCollageViewFactory() {
    collageViewFactory = CollageViewFactory(
      context = rootView.context,
      attrs = null,
      layoutWidth = collageFrame.width,
      layoutHeight = collageFrame.height,
      isBorderEnabled = false,
      imageUris = viewModel.imageUris)
  }

  private fun initOptionsButtons() {
    requireView().also {
      it.findViewById<CardView>(R.id.button_frame).setOnClickListener(this)
      it.findViewById<CardView>(R.id.button_aspect).setOnClickListener(this)
      it.findViewById<CardView>(R.id.button_toggle_border).setOnClickListener(this)
    }
  }


  // ------------------------ FINALIZATION ------------------------

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(IS_RETURN_SESSION, true)
    super.onSaveInstanceState(outState)
  }


  // ------------------------ COLLAGE VIEWS ------------------------

  private fun showCollageTypesInRecycler() {
    val adapter = CollageTypeListAdapter(requireContext(), viewModel.collageIconIdToType)

    adapter.setCollageTypeClickedListener(object : CollageTypeListAdapter.TypeClickedListener {
      override fun onCollageTypeClicked(collageType: CollageViewFactory.CollageType) {
        collage = collageViewFactory.getCollage(collageType)
        collageFrame.removeAllViews()
        collageFrame.addView(collage)
        collage.setImageClickListener(this@EditorFragment)
      }
    })

    requireView().findViewById<RecyclerView>(R.id.tool_popup_recycler).also {
      it.adapter = adapter
      it.layoutManager =
        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
  }


  // ------------------------ RATIOS ------------------------

  private fun showAspectRatiosInRecycler() {
    val adapter = RatioListAdapter(requireView().context, viewModel.ratioStringToValue)

    adapter.setButtonClickedListener(object : RatioListAdapter.RatioButtonClickedListener {
      override fun onRatioButtonClicked(ratio: Float) {
        viewModel.setRatio(ratio)
      }
    })

    requireView().findViewById<RecyclerView>(R.id.tool_popup_recycler).also {
      it.adapter = adapter
      it.layoutManager = LinearLayoutManager(
        requireView().context,
        LinearLayoutManager.HORIZONTAL,
        false)
    }
  }

  private fun onRatioChange(ratio: Float) {
    if (collageFrame.height > 0) {
      collage.setRatio(ratio)

      collageFrameParent.updateLayoutParams {
        this.width = ViewGroup.LayoutParams.WRAP_CONTENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
      }
    }
  }


  // ------------------------ BORDER ------------------------

  private fun showColorsInRecycler() {
    val adapter = ColorAdapter(requireView().context, rootView.width)

    adapter.setColorClickedListener(object : ColorAdapter.ColorClickedListener {
      override fun onColorClicked(color: Int) {
        collage.isBorderEnabled = true
        collage.setBorderColor(color)
      }
    })

    requireView().findViewById<RecyclerView>(R.id.tool_popup_recycler).also {
      it.adapter = adapter
      it.layoutManager = LinearLayoutManager(
        requireView().context,
        LinearLayoutManager.HORIZONTAL,
        false)
    }
  }

  private fun toggleBorder() {
    showColorsInRecycler()
    collage.toggleBorder()
  }


  // ------------------------ IMPORTING IMAGES ------------------------

  private fun startImageImportIntent() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, REQUEST_IMAGE_IMPORT_CODE)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_IMAGE_IMPORT_CODE -> {
        if (resultCode == RESULT_OK && data != null) onImageImported(data)
      }
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  private fun onImageImported(data: Intent) {
    val uri = data.data!!
    viewModel.addImageUri(uri, lastImageClickedIndex)

    if (collage.childCount > lastImageClickedIndex) {
      collage.setImageAt(lastImageClickedIndex, uri)
      lastImageClickedIndex = -1
    }
    else Log.d(TAG, "onImageImported: Selected TouchImageView no longer exists")
  }

  private fun loadImageIntoCollage(uri: Uri?, position: Int) {
    collage.setImageAt(position, uri)
  }

  override fun onClick(view: View?) {
    if (view is TouchImageView) {
      lastImageClickedIndex = collage.indexOfChild(view)
      startImageImportIntent()
    }

    when (view?.id) {
      R.id.button_frame -> showCollageTypesInRecycler()
      R.id.button_aspect -> showAspectRatiosInRecycler()
      R.id.button_toggle_border -> toggleBorder()
    }
  }

  companion object {
    private const val TAG = "EditorFragment"
    private const val REQUEST_IMAGE_IMPORT_CODE = 1000
    private const val IS_RETURN_SESSION = "is_continuing"
  }
}