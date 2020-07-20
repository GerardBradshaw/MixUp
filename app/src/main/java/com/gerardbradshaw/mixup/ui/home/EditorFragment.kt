package com.gerardbradshaw.mixup.ui.home

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.Toast
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.gerardbradshaw.mixup.R
import com.ortiz.touchview.TouchImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade

private const val REQUEST_IMAGE_IMPORT_CODE = 1000

class EditorFragment : Fragment() {
  private lateinit var editorViewModel: EditorViewModel
  private lateinit var rootView: View
  private lateinit var imageGrid: GridLayout
  private var selectedImageIndex: Int = 0
  private val LOG_TAG = EditorFragment::class.java.name
  private val imageUris = arrayOfNulls<Uri>(8)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    editorViewModel = ViewModelProvider(this).get(EditorViewModel::class.java)

    rootView = inflater.inflate(R.layout.fragment_editor, container, false)

    initUi()

    return rootView
  }

  private fun initUi() {
    initImageContainer()
    rootView.findViewById<Button>(R.id.button_frame).setOnClickListener { openFrameOptions() }
    rootView.findViewById<Button>(R.id.button_aspect).setOnClickListener { openAspectOptions() }
    rootView.findViewById<Button>(R.id.button_toggle_border).setOnClickListener { toggleBorder() }
  }

  private fun initImageContainer() {
    imageGrid = rootView
      .findViewById<FrameLayout>(R.id.image_container)
      .getChildAt(0) as GridLayout

    initFrameWithSavedImages()
    initImageClickListeners()
  }

  private fun initFrameWithSavedImages() {
    for (i in 0 until imageGrid.childCount) {
      if (imageUris[i] != null) {
        insertImageAtIndex(imageUris[i]!!, i)
      }
      else {
        val uri = Uri.parse(
          ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
              + resources.getResourcePackageName(R.drawable.img_click_to_add_photo) + '/'
              + resources.getResourceTypeName(R.drawable.img_click_to_add_photo) + '/'
              + resources.getResourceEntryName(R.drawable.img_click_to_add_photo))
        insertImageAtIndex(uri, i)
      }
    }
  }

  private fun initImageClickListeners() {
    for (i in 0 until imageGrid.childCount) {
      imageGrid.getChildAt(i).setOnClickListener { addImages(i) }
    }
  }

  private fun openFrameOptions() {
    val imageContainer = rootView.findViewById<FrameLayout>(R.id.image_container)

    imageContainer.removeAllViews()
    inflate(rootView.context, R.layout.frame_4img_0, imageContainer)

    initImageContainer()

    // TODO bring up menu
  }

  private fun openAspectOptions() {
    makeToast("Aspect not implemented")
    // TODO("Not implemented")
  }

  private fun toggleBorder() {
    val containerParams = imageGrid.layoutParams as ViewGroup.MarginLayoutParams
    val hasBorder = containerParams.marginStart <= 0
    val borderWidth = dpToPx(if (hasBorder) 6 else 0)

    containerParams.setMargins(borderWidth)
    imageGrid.layoutParams = containerParams

    for (i in 0 until imageGrid.childCount) {
      val imageParams = imageGrid.getChildAt(i).layoutParams as ViewGroup.MarginLayoutParams
      imageParams.setMargins(borderWidth)
      imageGrid.getChildAt(i).layoutParams = imageParams
    }
  }

  private fun dpToPx(dp: Int): Int {
    val dpFloat = dp.toFloat()

    return TypedValue
      .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpFloat, resources.displayMetrics)
      .toInt()
  }

  private fun addImages(childIndex: Int) {
    selectedImageIndex = childIndex
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, REQUEST_IMAGE_IMPORT_CODE)
  }

  private fun makeToast(message: String) {
    Toast.makeText(rootView.context, message, Toast.LENGTH_SHORT).show()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_IMAGE_IMPORT_CODE -> if (resultCode == RESULT_OK && data != null) onImageSelected(data)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  private fun onImageSelected(data: Intent) {
    val uri = data.data!!

    if (imageGrid.childCount > selectedImageIndex) {
      insertImageAtIndex(uri, selectedImageIndex)

      if (selectedImageIndex < imageUris.size) imageUris[selectedImageIndex] = uri
      else Log.d(LOG_TAG, "Cannot save uri. Index out of bounds")

      selectedImageIndex = 0
    }
    else Log.d(LOG_TAG, "Selected TouchImageView no longer exists")
  }

  private fun insertImageAtIndex(uri: Uri, index: Int) {
    val touchImageView = imageGrid.getChildAt(index) as TouchImageView
    Glide
      .with(this)
      .load(uri)
      .transition(withCrossFade())
      .into(touchImageView)
  }
}