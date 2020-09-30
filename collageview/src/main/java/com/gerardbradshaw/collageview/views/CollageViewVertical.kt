package com.gerardbradshaw.collageview.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import com.gerardbradshaw.collageview.util.ImageParams

class CollageViewVertical(context: Context, attrs: AttributeSet?, imageCount: Int,
                          totalWidth: Int, totalHeight: Int,
                          isBorderEnabled: Boolean = false,
                          imageUris: Array<Uri?>? = null) :
  AbstractCollageView(context, attrs, imageCount,
    totalWidth, totalHeight, isBorderEnabled, imageUris),
  View.OnTouchListener {

  // ------------------------ INITIALIZATION ------------------------

  // Do not change
  init {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        if (height > 0) {
          addViewsToLayout()
          initImageLayout()
          addImagesToViews()
          prepareTouchListeners()
          enableBorder(isBorderEnabled)

          isFrameInflated = true
          viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
      }
    })
  }

  private fun initImageLayout() {
    val height = layoutHeight.toFloat() / imageCount().toFloat()

    for (i in imageViews.indices) {
      imageParamsCache[i] = ImageParams(layoutWidth.toFloat(), height, 0f, i * height)
    }

    syncLayoutWithParamCache()
  }

  // Do not change
  private fun prepareTouchListeners() {
    for (view in imageViews) view.setOnTouchListener(this)
  }


  // ------------------------ RESIZING HELPERS ------------------------

  private fun resizeImageAt(index: Int, deltaWidth: Float, deltaHeight: Float) {
    when (touchedImageEdge) {
      null -> {
        Log.d(TAG, "resizeImageAt: invalid edge")
      }

      Edge.BOTTOM_LEFT_CORNER, Edge.BOTTOM_SIDE, Edge.BOTTOM_RIGHT_CORNER -> {
        if (index == imageViews.size - 1) return

        val okToAdjustHeight = imageParamsCache[index].height + deltaHeight > minDimension &&
            imageParamsCache[index + 1].height - deltaHeight > minDimension

        if (!okToAdjustHeight) Log.d(TAG, "resizeImageAt: unsafe to resize")
        else {
          imageParamsCache[index].height += deltaHeight

          imageParamsCache[index + 1].height -= deltaHeight
          imageParamsCache[index + 1].y += deltaHeight

          syncLayoutWithParamCache()
        }
      }

      Edge.TOP_LEFT_CORNER, Edge.TOP_SIDE, Edge.TOP_RIGHT_CORNER -> {
        if (index == 0) return

        val okToAdjustHeight = imageParamsCache[index].height - deltaHeight > minDimension &&
            imageParamsCache[index - 1].height + deltaHeight > minDimension

        if (!okToAdjustHeight) Log.d(TAG, "resizeImageAt: unsafe to resize")
        else {
          imageParamsCache[index].height -= deltaHeight
          imageParamsCache[index].y += deltaHeight

          imageParamsCache[index - 1].height += deltaHeight

          syncLayoutWithParamCache()
        }
      }

      else -> return
    }
  }


  // ------------------------ TOUCH HELPER ------------------------

  // Do not change
  override fun onTouch(v: View, event: MotionEvent): Boolean {
    return onTouchHelper(v, event, ::resizeImageAt)
  }


  // ------------------------ HELPERS ------------------------

  companion object {
    private const val TAG = "CollageViewVertical"
  }
}