package com.gerardbradshaw.mixup.ui.editor

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.collageview.CollageViewFactory
import com.gerardbradshaw.collageview.views.AbstractCollageView
import com.gerardbradshaw.library.views.AbstractColorPickerView
import com.gerardbradshaw.library.views.CompactColorPickerView
import com.gerardbradshaw.mixup.BaseApplication
import com.gerardbradshaw.mixup.R
import com.ortiz.touchview.TouchImageView

class EditorFragment : Fragment(), View.OnClickListener, AbstractColorPickerView.ColorChangedListener {

  private lateinit var rootView: View
  private lateinit var viewModel: EditorViewModel
  private lateinit var collageViewFactory: CollageViewFactory

  private lateinit var collageViewContainerParent: FrameLayout
  private lateinit var collageViewContainer: FrameLayout
  private lateinit var collageView: AbstractCollageView

  private lateinit var recyclerView: RecyclerView
  private lateinit var colorPickerContainer: LinearLayout
  private lateinit var borderSwitch: SwitchCompat

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

    initOptionsButtons()
    initTools()
    initCollage()
    initBorderColorPicker()

    viewModel.canvasAspectRatio.observe(requireActivity(), Observer { onAspectRatioChange(it) })
  }

  private fun initTools() {
    recyclerView = requireView().findViewById(R.id.tool_popup_recycler)
    colorPickerContainer = requireView().findViewById(R.id.color_picker_container)

    showCollageLayoutsInRecycler()
  }

  private fun initCollage() {
    collageViewContainerParent = rootView.findViewById(R.id.collage_container_parent)

    collageViewContainer = rootView.findViewById(R.id.collage_container)

    collageViewContainer.viewTreeObserver.addOnGlobalLayoutListener(
      object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
          if (collageViewContainer.height > 0) {
            initCollageViewFactory()
            initDefaultCollageView()

            collageViewContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
          }
        }
      })
  }

  private fun initCollageViewFactory() {
    collageViewFactory = CollageViewFactory(
      context = rootView.context,
      attrs = null,
      layoutWidth = collageViewContainer.width,
      layoutHeight = collageViewContainer.height,
      isBorderEnabled = false,
      imageUris = viewModel.imageUris)
  }

  private fun initDefaultCollageView() {
    collageView = collageViewFactory.getView(CollageViewFactory.CollageLayoutType.THREE_IMAGE_2)

    collageViewContainer.addView(collageView)

    collageView.setImageClickListener(this)
  }

  private fun initOptionsButtons() {
    requireView().also {
      it.findViewById<CardView>(R.id.button_layout).setOnClickListener(this)
      it.findViewById<CardView>(R.id.button_aspect_ratio).setOnClickListener(this)
      it.findViewById<CardView>(R.id.button_toggle_border).setOnClickListener(this)
    }
  }

  private fun initBorderColorPicker() {
    val colorPicker: CompactColorPickerView = requireView().findViewById(R.id.slide_view)
    colorPicker.setOnColorSelectedListener(this)

    borderSwitch = requireView().findViewById(R.id.border_switch)
    borderSwitch.setOnCheckedChangeListener { _, isChecked ->
      collageView.enableBorder(isChecked)
      collageView.setBorderColor(colorPicker.getCurrentColor())
    }
  }

  private fun setIsColorPickerHidden(hideColorPicker: Boolean) {
    if (hideColorPicker) {
      colorPickerContainer.visibility = View.GONE
      recyclerView.visibility = View.VISIBLE
    }
    else {
      colorPickerContainer.visibility = View.VISIBLE
      recyclerView.visibility = View.GONE
    }
  }



  // ------------------------ COLLAGE ------------------------

  private fun showCollageLayoutsInRecycler() {
    val adapter = CollageLayoutListAdapter(requireContext(), viewModel.collageIconIdToType)

    adapter.setCollageTypeClickedListener(object : CollageLayoutListAdapter.TypeClickedListener {
      override fun onLayoutTypeClicked(collageLayoutType: CollageViewFactory.CollageLayoutType) {
        onNewCollageTypeSelected(collageLayoutType)
      }
    })

    recyclerView.adapter = adapter

    recyclerView.layoutManager = LinearLayoutManager(
      requireContext(), LinearLayoutManager.HORIZONTAL, false)

    setIsColorPickerHidden(true)
  }

  private fun onNewCollageTypeSelected(collageLayoutType: CollageViewFactory.CollageLayoutType) {
    collageView = collageViewFactory.getView(collageLayoutType)
    collageViewContainer.removeAllViews()
    collageViewContainer.addView(collageView)
    collageView.setImageClickListener(this@EditorFragment)
  }

  fun reset() {
    viewModel.resetImageUris()
    collageView.reset()
    borderSwitch.isChecked = false
  }



  // ------------------------ ASPECT RATIO ------------------------

  private fun showAspectRatiosInRecycler() {
    val adapter = AspectRatioListAdapter(requireView().context, viewModel.ratioStringToValue)

    adapter.setButtonClickedListener(object : AspectRatioListAdapter.AspectRatioButtonClickedListener {
      override fun onAspectRatioButtonClicked(newRatio: Float) {
        viewModel.setAspectRatio(newRatio)
      }
    })

    recyclerView.adapter = adapter

    recyclerView.layoutManager = LinearLayoutManager(
      requireView().context, LinearLayoutManager.HORIZONTAL, false)

    setIsColorPickerHidden(true)
  }

  private fun onAspectRatioChange(newRatio: Float) {
    if (collageViewContainer.height > 0) {
      collageView.aspectRatio = newRatio

      collageViewContainerParent.updateLayoutParams {
        this.width = ViewGroup.LayoutParams.WRAP_CONTENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
      }
    }
  }



  // ------------------------ BORDER ------------------------

  private fun showBorderOptions() {
    setIsColorPickerHidden(false)
  }

  override fun onColorChanged(color: Int) {
    collageView.isBorderEnabled = true
    if (!borderSwitch.isChecked) borderSwitch.isChecked = true
    collageView.setBorderColor(color)
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

    if (collageView.childCount > lastImageClickedIndex) {
      collageView.setImageAt(lastImageClickedIndex, uri)
      lastImageClickedIndex = -1
    }
    else Log.d(TAG, "onImageImported: Selected TouchImageView no longer exists")
  }

  override fun onClick(view: View?) {
    if (view is TouchImageView) {
      lastImageClickedIndex = collageView.indexOfChild(view)
      startImageImportIntent()
    }

    when (view?.id) {
      R.id.button_layout -> showCollageLayoutsInRecycler()
      R.id.button_aspect_ratio -> showAspectRatiosInRecycler()
      R.id.button_toggle_border -> showBorderOptions()
    }
  }

  companion object {
    private const val TAG = "EditorFragment"
    private const val REQUEST_IMAGE_IMPORT_CODE = 1000
  }
}