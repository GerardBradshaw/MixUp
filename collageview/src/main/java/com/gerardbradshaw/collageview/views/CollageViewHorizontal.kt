package com.gerardbradshaw.collageview.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import com.gerardbradshaw.collageview.util.ImageParams

class CollageViewHorizontal(context: Context, attrs: AttributeSet?, imageCount: Int,
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

          isLayoutInflated = true
          viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
      }
    })
  }

  override fun initImageLayout() {
    val width = layoutWidth.toFloat() / imageCount().toFloat()

    for (i in imageViews.indices) {
      imageParamsCache[i] = ImageParams(width, layoutHeight.toFloat(), i * width, 0f)
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

      Edge.TOP_RIGHT_CORNER, Edge.RIGHT_SIDE, Edge.BOTTOM_RIGHT_CORNER -> {
        if (index == imageViews.size - 1) return

        val okToAdjustWidth = imageParamsCache[index].width + deltaWidth > minDimension &&
            imageParamsCache[index + 1].width - deltaWidth > minDimension

        if (!okToAdjustWidth) Log.d(TAG, "resizeImageAt: unsafe to resize")
        else {
          imageParamsCache[index].width += deltaWidth

          imageParamsCache[index + 1].width -= deltaWidth
          imageParamsCache[index + 1].x += deltaWidth

          syncLayoutWithParamCache()
        }
      }

      Edge.TOP_LEFT_CORNER, Edge.LEFT_SIDE, Edge.BOTTOM_LEFT_CORNER -> {
        if (index == 0) return

        val okToAdjustWidth = imageParamsCache[index].width - deltaWidth > minDimension &&
            imageParamsCache[index - 1].width + deltaWidth > minDimension

        if (!okToAdjustWidth) Log.d(TAG, "resizeImageAt: unsafe to resize")
        else {
          imageParamsCache[index].width -= deltaWidth
          imageParamsCache[index].x += deltaWidth

          imageParamsCache[index - 1].width += deltaWidth

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
    private const val TAG = "CollageViewHorizontal"
  }
}