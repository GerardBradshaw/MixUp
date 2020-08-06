package com.gerardbradshaw.mixup.ui.editor

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.models.CanvasInfo
import com.ortiz.touchview.TouchImageView
import kotlin.math.max

private const val REQUEST_IMAGE_IMPORT_CODE = 1000
private const val LOG_TAG = "EditorFragment"
private const val RATIO = "ratio"

class EditorFragment : Fragment(), View.OnClickListener {

  private lateinit var editorViewModel: EditorViewModel
  private lateinit var imageGrid: GridLayout
  private lateinit var defaultImageUri: Uri
  private val canvasInfo = CanvasInfo()
  private var lastSelectedImagePos: Int = 0

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_editor, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    editorViewModel = ViewModelProvider(this).get(EditorViewModel::class.java)
    defaultImageUri = Uri.parse(
      ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
          + resources.getResourcePackageName(R.drawable.img_tap_to_add_photo) + '/'
          + resources.getResourceTypeName(R.drawable.img_tap_to_add_photo) + '/'
          + resources.getResourceEntryName(R.drawable.img_tap_to_add_photo))

    if (savedInstanceState != null && savedInstanceState.containsKey(RATIO)) {
      canvasInfo.ratio = savedInstanceState.getFloat(RATIO)
    }

    initCanvasInfo()
    initUi()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putFloat(RATIO, canvasInfo.ratio)
    super.onSaveInstanceState(outState)
  }

  private fun initCanvasInfo() {
    canvasInfo.imageUris = editorViewModel.getImageUris()

    val canvas = requireView().findViewById<FrameLayout>(R.id.image_card_view)
    canvas.post {
      canvasInfo.height = canvas.height.toFloat()
      canvasInfo.width = canvas.width.toFloat()
    }
  }

  private fun initUi() {
    initFrame()
    initButtons()
    initRecyclerWithFrames()
  }

  private fun initFrame() {
    imageGrid = requireView().findViewById<FrameLayout>(R.id.image_container)
      .getChildAt(0) as GridLayout

    updateAspectRatioOfFrame(canvasInfo.ratio)
    setPhotosInFrame()
    setClickListenersForPhotosInFrame()
  }

  private fun initButtons() {
    requireView().also {
      it.findViewById<CardView>(R.id.button_frame).setOnClickListener(this)
      it.findViewById<CardView>(R.id.button_aspect).setOnClickListener(this)
      it.findViewById<CardView>(R.id.button_toggle_border).setOnClickListener(this)
    }
  }

  private fun initRecyclerWithFrames() {
    val frameIconIdToLayoutId = editorViewModel.getFrameIconIdToLayoutMap()
    val adapter = FrameListAdapter(requireView().context, frameIconIdToLayoutId)

    adapter.setButtonClickedListener(object : FrameListAdapter.ToolButtonClickedListener {
      override fun onToolButtonClicked(resId: Int?) {
        if (resId == null) {
          Log.d(LOG_TAG, "Resource ID for selected frame was null! Check recycler adapter.")
          return
        }

        try {
          requireView().context.resources.getResourceName(resId)
          val imageContainer = requireView().findViewById<FrameLayout>(R.id.image_container)
          imageContainer.removeAllViews()
          inflate(requireView().context, resId, imageContainer)
          initFrame()

        } catch (e: Resources.NotFoundException) {
          Log.d(LOG_TAG, "Invalid resource ID for selected frame. Res ID = $resId.}")
        }
      }
    })

    requireView().findViewById<RecyclerView>(R.id.tool_option_recycler).also {
      it.adapter = adapter
      it.layoutManager =
        LinearLayoutManager(requireView().context, LinearLayoutManager.HORIZONTAL, false)
    }
  }

  private fun initRecyclerWithRatios() {
    val ratioStringToValue = editorViewModel.getRatioStringToValueMap()
    val adapter = RatioListAdapter(requireView().context, ratioStringToValue)

    adapter.setButtonClickedListener(object : RatioListAdapter.RatioButtonClickedListener {
      override fun onRatioButtonClicked(ratio: Float?) {
        if (ratio == null) Log.d(LOG_TAG, "Ratio was null!")
        else updateAspectRatioOfFrame(ratio)
      }
    })

    requireView().findViewById<RecyclerView>(R.id.tool_option_recycler).also {
      it.adapter = adapter
      it.layoutManager = LinearLayoutManager(
        requireView().context,
        LinearLayoutManager.HORIZONTAL,
        false)
    }
  }

  private fun updateAspectRatioOfFrame(ratio: Float) {
    canvasInfo.ratio = ratio
    val imageCard = requireView().findViewById<FrameLayout>(R.id.image_card_view)

    imageCard.post {
      var xMargin = resources.getDimensionPixelSize(R.dimen.image_init_margin).toFloat()
      var yMargin = resources.getDimensionPixelSize(R.dimen.image_init_margin).toFloat()

      val adjustHeight = canvasInfo.height > canvasInfo.width / ratio

      if (adjustHeight) {
        val newHeight = canvasInfo.width / ratio
        yMargin += (canvasInfo.height - newHeight) / 2f

      } else {
        val newWidth = canvasInfo.height * ratio
        xMargin += (canvasInfo.width - newWidth) / 2f
      }
      updateMarginsOfView(
        imageCard, xMargin.toInt(), yMargin.toInt(), xMargin.toInt(), yMargin.toInt())
    }
  }

  private fun setPhotosInFrame() {
    for (i in 0 until imageGrid.childCount) {
      insertImageInFrame(canvasInfo.imageUris[i], i)
    }
  }

  private fun setClickListenersForPhotosInFrame() {
    for (i in 0 until imageGrid.childCount) {
      imageGrid.getChildAt(i).setOnClickListener {
        lastSelectedImagePos = i
        openGalleryToSelectImage()
      }
    }
  }

  private fun openFrameOptions() {
    initRecyclerWithFrames()
  }

  private fun openAspectOptions() {
    initRecyclerWithRatios()
  }

  private fun toggleBorder() {
    val largestDimension = max(canvasInfo.height, canvasInfo.width).toInt()
    val maxBorderThicknessPx = largestDimension / 150
    val hasBorder = imageGrid.paddingStart <= 0

    val thickness = if (hasBorder) maxBorderThicknessPx else 0
    imageGrid.setPadding(thickness)

    for (i in 0 until imageGrid.childCount) {
      updateMarginsOfView(imageGrid.getChildAt(i), thickness, thickness, thickness, thickness)
    }
  }

  private fun updateMarginsOfView(view: View, leftMargin: Int, topMargin: Int,
                                  rightMargin: Int, bottomMargin: Int) {
    val params = view.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
    view.layoutParams = params
  }

  private fun openGalleryToSelectImage() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, REQUEST_IMAGE_IMPORT_CODE)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_IMAGE_IMPORT_CODE -> if (resultCode == RESULT_OK && data != null) onImageSelected(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  private fun onImageSelected(data: Intent) {
    val uri = data.data!!

    if (imageGrid.childCount > lastSelectedImagePos) {
      insertImageInFrame(uri, lastSelectedImagePos)
      editorViewModel.addImageUri(uri, lastSelectedImagePos)
      lastSelectedImagePos = 0
    }
    else Log.d(LOG_TAG, "Selected TouchImageView no longer exists")
  }

  private fun insertImageInFrame(uri: Uri?, position: Int) {
    val touchImageView = imageGrid.getChildAt(position) as TouchImageView

    if (uri != null) touchImageView.scaleType = ImageView.ScaleType.CENTER
    else touchImageView.scaleType = ImageView.ScaleType.FIT_CENTER

    Glide
      .with(this)
      .load(uri ?: defaultImageUri)
      .transition(withCrossFade())
      .into(touchImageView)
  }

  override fun onClick(view: View?) {
    if (view == null) return

    when (view.id) {
      R.id.button_frame -> openFrameOptions()
      R.id.button_aspect -> openAspectOptions()
      R.id.button_toggle_border -> toggleBorder()
    }
  }
}