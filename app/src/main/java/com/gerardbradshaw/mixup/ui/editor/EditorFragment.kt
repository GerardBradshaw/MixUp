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
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.gerardbradshaw.mixup.R
import com.ortiz.touchview.TouchImageView

private const val REQUEST_IMAGE_IMPORT_CODE = 1000
private const val DEBUG_LOG_TAG = "EditorFragment"

class EditorFragment : Fragment() {
  private lateinit var editorViewModel: EditorViewModel
  private lateinit var frameIconIdToLayoutId: HashMap<Int, Int>
  private lateinit var ratioStringToValue: HashMap<String, Float>
  private lateinit var rootView: View
  private lateinit var imageContainer: GridLayout
  private lateinit var imageUris: Array<Uri?>
  private var selectedImagePosition: Int = 0

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    editorViewModel = ViewModelProvider(this).get(EditorViewModel::class.java)
    rootView = inflater.inflate(R.layout.fragment_editor, container, false)

    initData()
    initUi()

    return rootView
  }

  private fun initData() {
    frameIconIdToLayoutId = editorViewModel.getFrameIconIdToLayoutMap()
    ratioStringToValue = editorViewModel.getRatioStringToValueMap()
    imageUris = editorViewModel.getImageUris()
  }

  private fun initUi() {
    initFrame()
    initToolButtons()
    initToolOptionsRecycler()
  }

  private fun initFrame() {
    imageContainer = rootView.findViewById<FrameLayout>(R.id.image_container)
      .getChildAt(0) as GridLayout

    initAspectRatioOfFrame()
    setPhotosInFrame()
    setClickListenersForPhotosInFrame()
  }

  private fun initToolButtons() {
    rootView.findViewById<CardView>(R.id.button_frame).setOnClickListener { openFrameOptions() }
    rootView.findViewById<CardView>(R.id.button_aspect).setOnClickListener { openAspectOptions() }
    rootView.findViewById<CardView>(R.id.button_toggle_border).setOnClickListener { toggleBorder() }
  }

  private fun initToolOptionsRecycler() {
    val adapter = FrameListAdapter(rootView.context, frameIconIdToLayoutId)

    adapter.setButtonClickedListener(object : FrameListAdapter.ToolButtonClickedListener {
      override fun onToolButtonClicked(resId: Int?) {
        if (resId == null) {
          Log.d(DEBUG_LOG_TAG, "Resource ID for selected frame was null! Check recycler adapter.")
          return
        }

        try {
          rootView.context.resources.getResourceName(resId)
          val imageContainer = rootView.findViewById<FrameLayout>(R.id.image_container)
          imageContainer.removeAllViews()
          inflate(rootView.context, resId, imageContainer)
          initFrame()

        } catch (e: Resources.NotFoundException) {
          Log.d(DEBUG_LOG_TAG, "Invalid resource ID for selected frame. Res ID = $resId.}")
        }
      }
    })

    rootView.findViewById<RecyclerView>(R.id.tool_option_recycler).also {
      it.adapter = adapter
      it.layoutManager =
        LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
    }
  }

  private fun initAspectRatioOfFrame() {
    val imageCard = rootView.findViewById<FrameLayout>(R.id.image_card_view)
    imageCard.post {
      val maxImageHeight = imageCard.height.toFloat()
      val maxImageWidth = imageCard.width.toFloat()

      var xMargin = resources.getDimensionPixelSize(R.dimen.image_init_margin).toFloat()
      var yMargin = resources.getDimensionPixelSize(R.dimen.image_init_margin).toFloat()

      val ratio = 3f / 4f
      val shouldAdjustHeight = maxImageHeight > maxImageWidth / ratio

      if (shouldAdjustHeight) {
        val newHeight = maxImageWidth / ratio
        yMargin += (maxImageHeight - newHeight) / 2f

      } else {
        val newWidth = maxImageHeight * ratio
        xMargin += (maxImageWidth - newWidth) / 2f
      }
      updateMarginsOfView(
        imageCard, xMargin.toInt(), yMargin.toInt(), xMargin.toInt(), yMargin.toInt())
    }
  }

  private fun setPhotosInFrame() {
    val defaultUri = Uri.parse(
      ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
          + resources.getResourcePackageName(R.drawable.ic_tap_to_add_photo) + '/'
          + resources.getResourceTypeName(R.drawable.ic_tap_to_add_photo) + '/'
          + resources.getResourceEntryName(R.drawable.ic_tap_to_add_photo))

    for (i in 0 until imageContainer.childCount) {
      val uriForImageAtI = if (imageUris[i] != null) imageUris[i]!! else defaultUri
      insertImageInFrame(uriForImageAtI, i)
    }
  }

  private fun setClickListenersForPhotosInFrame() {
    for (i in 0 until imageContainer.childCount) {
      imageContainer.getChildAt(i).setOnClickListener {
        selectedImagePosition = i
        openGalleryToSelectImage()
      }
    }
  }

  private fun openFrameOptions() {
    initToolOptionsRecycler()
  }

  private fun openAspectOptions() {
    // TODO("Not implemented")
  }

  private fun toggleBorder() {
    val hasBorder = imageContainer.paddingStart <= 0
    val thickness = if (hasBorder) resources.getDimensionPixelSize(R.dimen.border_thickness) else 0
    imageContainer.setPadding(thickness)

    for (i in 0 until imageContainer.childCount) {
      updateMarginsOfView(imageContainer.getChildAt(i), thickness, thickness, thickness, thickness)
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

    if (imageContainer.childCount > selectedImagePosition) {
      insertImageInFrame(uri, selectedImagePosition)
      editorViewModel.addImageUri(uri, selectedImagePosition)
      selectedImagePosition = 0
    }
    else Log.d(DEBUG_LOG_TAG, "Selected TouchImageView no longer exists")
  }

  private fun insertImageInFrame(uri: Uri, position: Int) {
    val touchImageView = imageContainer.getChildAt(position) as TouchImageView
    Glide
      .with(this)
      .load(uri)
      .transition(withCrossFade())
      .into(touchImageView)
  }
}