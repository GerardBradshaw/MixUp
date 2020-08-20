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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.gerardbradshaw.mixup.BaseApplication
import com.gerardbradshaw.mixup.R
import com.ortiz.touchview.TouchImageView
import javax.inject.Inject
import kotlin.math.max

private const val TAG = "EditorFragment"
private const val REQUEST_IMAGE_IMPORT_CODE = 1000
private const val IS_RETURN_SESSION = "is_continuing"

class EditorFragment : Fragment(), View.OnClickListener {

  @Inject lateinit var glideInstance: RequestManager
  private lateinit var viewModel: EditorViewModel
  private lateinit var collage: GridLayout
  private var lastSelectedImagePos: Int = -1


  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {

    (requireActivity().application as BaseApplication).getAppComponent()
      .editorComponent().create().inject(this)

    return inflater.inflate(R.layout.fragment_editor, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    viewModel = ViewModelProvider(this).get(EditorViewModel::class.java)

    if (savedInstanceState == null || !savedInstanceState.getBoolean(IS_RETURN_SESSION)) {
      initData()
    }

    viewModel.canvasRatio.observe(requireActivity(), Observer { onRatioChange(it) })

    initUi()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(IS_RETURN_SESSION, true)
    super.onSaveInstanceState(outState)
  }

  private fun initData() {
    viewModel.defaultImageUri = Uri.parse(
      ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
          + resources.getResourcePackageName(R.drawable.img_tap_to_add_photo) + '/'
          + resources.getResourceTypeName(R.drawable.img_tap_to_add_photo) + '/'
          + resources.getResourceEntryName(R.drawable.img_tap_to_add_photo))

    val canvas = requireView().findViewById<FrameLayout>(R.id.image_card_view)
    canvas.post {
      viewModel.maxHeight = canvas.height.toFloat()
      viewModel.maxWidth = canvas.width.toFloat()
    }
  }

  private fun initUi() {
    initCollage()
    initOptionsButtons()
    showFramesInRecycler()
  }

  private fun initCollage() {
    collage = requireView().findViewById<FrameLayout>(R.id.image_container)
      .getChildAt(0) as GridLayout

    loadPhotosIntoCollage()
    setCollageClickListeners()
  }

  private fun initOptionsButtons() {
    requireView().also {
      it.findViewById<CardView>(R.id.button_frame).setOnClickListener(this)
      it.findViewById<CardView>(R.id.button_aspect).setOnClickListener(this)
      it.findViewById<CardView>(R.id.button_toggle_border).setOnClickListener(this)
    }
  }

  private fun showFramesInRecycler() {
    val adapter = FrameListAdapter(requireContext(), viewModel.frameIconIdToLayoutId)

    adapter.setButtonClickedListener(object : FrameListAdapter.ToolButtonClickedListener {
      override fun onToolButtonClicked(resId: Int) {
        try {
          requireView().findViewById<FrameLayout>(R.id.image_container).also {
            it.removeAllViews()
            inflate(requireContext(), resId, it)
            initCollage()
          }
        } catch (e: Resources.NotFoundException) {
          Log.d(TAG, "onToolButtonClicked: Invalid resId for selected frame. ID: $resId}")
        }
      }
    })

    requireView().findViewById<RecyclerView>(R.id.tool_option_recycler).also {
      it.adapter = adapter
      it.layoutManager =
        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
  }

  private fun showAspectRatiosInRecycler() {
    val adapter = RatioListAdapter(requireView().context, viewModel.ratioStringToValue)

    adapter.setButtonClickedListener(object : RatioListAdapter.RatioButtonClickedListener {
      override fun onRatioButtonClicked(ratio: Float) {
        viewModel.setRatio(ratio)
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

  private fun onRatioChange(ratio: Float) {
    requireView().findViewById<FrameLayout>(R.id.image_card_view).also {
      it.post {
        var xMargin = resources.getDimensionPixelSize(R.dimen.image_init_margin).toFloat()
        var yMargin = resources.getDimensionPixelSize(R.dimen.image_init_margin).toFloat()

        val shouldAdjustY = viewModel.maxHeight > viewModel.maxWidth / ratio

        if (shouldAdjustY) yMargin += (viewModel.maxHeight - (viewModel.maxWidth / ratio)) / 2f
        else xMargin += (viewModel.maxWidth - (viewModel.maxHeight * ratio)) / 2f

        updateMarginsOfView(it, xMargin.toInt(), yMargin.toInt(), xMargin.toInt(), yMargin.toInt())
      }
    }
  }

  private fun loadPhotosIntoCollage() {
    for (i in 0 until collage.childCount) {
      loadImageIntoCollage(viewModel.imageUris[i], i)
    }
  }

  private fun setCollageClickListeners() {
    for (i in 0 until collage.childCount) {
      collage.getChildAt(i).setOnClickListener {
        lastSelectedImagePos = i
        startImageImportIntent()
      }
    }
  }

  private fun toggleBorder() {
    val largestDimension = max(viewModel.maxHeight, viewModel.maxWidth).toInt()
    val maxBorderWidthPx = largestDimension / 150

    val collageHasBorder = collage.paddingStart <= 0

    val width = if (collageHasBorder) maxBorderWidthPx else 0
    collage.setPadding(width)

    for (i in 0 until collage.childCount) {
      updateMarginsOfView(collage.getChildAt(i), width, width, width, width)
    }
  }

  private fun updateMarginsOfView(view: View, left: Int, top: Int, right: Int, bottom: Int) {
    val params = view.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(left, top, right, bottom)
    view.layoutParams = params
  }

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

    if (collage.childCount > lastSelectedImagePos) {
      loadImageIntoCollage(uri, lastSelectedImagePos)

      viewModel.addImageUri(uri, lastSelectedImagePos)

      lastSelectedImagePos = -1
    }
    else Log.d(TAG, "onImageImported: Selected TouchImageView no longer exists")
  }

  private fun loadImageIntoCollage(uri: Uri?, position: Int) {
    (collage.getChildAt(position) as TouchImageView).also {
      it.scaleType =
        if (uri != null) ImageView.ScaleType.CENTER
        else ImageView.ScaleType.FIT_CENTER

      glideInstance
        .load(uri ?: viewModel.defaultImageUri)
        .transition(withCrossFade())
        .into(it)
    }
  }

  override fun onClick(view: View?) {
    when (view?.id) {
      R.id.button_frame -> showFramesInRecycler()
      R.id.button_aspect -> showAspectRatiosInRecycler()
      R.id.button_toggle_border -> toggleBorder()
    }
  }
}