package com.gerardbradshaw.mixup.ui.editor

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
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

class EditorFragment : Fragment() {
  private lateinit var editorViewModel: EditorViewModel
  private lateinit var rootView: View
  private lateinit var imageGrid: GridLayout
  private var selectedImageIndex: Int = 0
  private val LOG_TAG = EditorFragment::class.java.name
  private val imageUris = arrayOfNulls<Uri>(8)
  private val frameResIdImgToLayout = HashMap<Int, Int>()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {

    editorViewModel = ViewModelProvider(this).get(EditorViewModel::class.java)
    rootView = inflater.inflate(R.layout.fragment_editor, container, false)
    initUi()
    return rootView
  }

  private fun initUi() {
    populateFrameMap()
    initImageContainer()
    initRecycler()
    rootView.findViewById<CardView>(R.id.button_frame).setOnClickListener { openFrameOptions() }
    rootView.findViewById<CardView>(R.id.button_aspect).setOnClickListener { openAspectOptions() }
    rootView.findViewById<CardView>(R.id.button_toggle_border).setOnClickListener { toggleBorder() }
  }

  private fun initRecycler() {
    val adapter = ToolListAdapter(rootView.context, frameResIdImgToLayout)

    adapter.setButtonClickedListener(object : ToolListAdapter.ToolButtonClickedListener {
      override fun onToolButtonClicked(resId: Int?) {
        if (resId == null) {
          Log.d(LOG_TAG, "Resource ID for selected frame was null! Check recycler adapter.")
          return
        }

        try {
          rootView.context.resources.getResourceName(resId)
          val imageContainer = rootView.findViewById<FrameLayout>(R.id.image_container)
          imageContainer.removeAllViews()
          inflate(rootView.context, resId, imageContainer)
          initImageContainer()

        } catch (e: Resources.NotFoundException) {
          Log.d(LOG_TAG, "Invalid resource ID for selected frame. Res ID = $resId.}")
        }
      }
    })

    rootView.findViewById<RecyclerView>(R.id.tool_option_recycler).also {
      it.adapter = adapter
      it.layoutManager =
        LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
    }
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
              + resources.getResourcePackageName(R.drawable.ic_tap_to_add_photo) + '/'
              + resources.getResourceTypeName(R.drawable.ic_tap_to_add_photo) + '/'
              + resources.getResourceEntryName(R.drawable.ic_tap_to_add_photo))
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
    inflate(rootView.context, R.layout.frame_4img_1, imageContainer)

    initImageContainer()

    // TODO bring up menu
  }

  private fun populateFrameMap() {
    frameResIdImgToLayout[R.drawable.frame_2img_0] = R.layout.frame_2img_0
    frameResIdImgToLayout[R.drawable.frame_2img_1] = R.layout.frame_2img_1
    frameResIdImgToLayout[R.drawable.frame_3img_0] = R.layout.frame_3img_0
    frameResIdImgToLayout[R.drawable.frame_3img_1] = R.layout.frame_3img_1
    frameResIdImgToLayout[R.drawable.frame_3img_2] = R.layout.frame_3img_2
    frameResIdImgToLayout[R.drawable.frame_3img_3] = R.layout.frame_3img_3
    frameResIdImgToLayout[R.drawable.frame_3img_4] = R.layout.frame_3img_4
    frameResIdImgToLayout[R.drawable.frane_3img_5] = R.layout.frame_3img_5
    frameResIdImgToLayout[R.drawable.frame_4img_0] = R.layout.frame_4img_0
    frameResIdImgToLayout[R.drawable.frame_4img_1] = R.layout.frame_4img_1
    frameResIdImgToLayout[R.drawable.frame_4img_2] = R.layout.frame_4img_2
    frameResIdImgToLayout[R.drawable.frame_4img_3] = R.layout.frame_4img_3
    frameResIdImgToLayout[R.drawable.frame_4img_4] = R.layout.frame_4img_4
  }

  private fun openAspectOptions() {
    // TODO("Not implemented")
  }

  private fun toggleBorder() {
    val hasBorder = imageGrid.paddingStart <= 0
    val borderWidth = dpToPx(if (hasBorder) 6 else 0)
    imageGrid.setPadding(borderWidth)

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