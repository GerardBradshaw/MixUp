package com.gerardbradshaw.collageview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import com.gerardbradshaw.collageview.views.*

class CollageViewFactory(var context: Context,
                         var attrs: AttributeSet?,
                         var layoutWidth: Int,
                         var layoutHeight: Int,
                         var isBorderEnabled: Boolean,
                         var imageUris: Array<Uri?>?) {

  fun getCollage(type: CollageType): AbstractCollageView {
    return when (type) {
      CollageType.TWO_IMAGE_VERTICAL -> CollageViewVertical(
        context,
        attrs,
        2,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.TWO_IMAGE_HORIZONTAL -> CollageViewHorizontal(
        context,
        attrs,
        2,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.THREE_IMAGE_0 -> CollageView3Image0(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.THREE_IMAGE_1 -> CollageView3Image1(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.THREE_IMAGE_2 -> CollageView3Image2(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.THREE_IMAGE_3 -> CollageView3Image3(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.THREE_IMAGE_VERTICAL -> CollageViewVertical(
        context,
        attrs,
        3,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.THREE_IMAGE_HORIZONTAL -> CollageViewHorizontal(
        context,
        attrs,
        3,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.FOUR_IMAGE_0 -> CollageView4Image0(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.FOUR_IMAGE_1 -> CollageView4Image1(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.FOUR_IMAGE_2 -> CollageView4Image2(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.FOUR_IMAGE_3 -> CollageView4Image3(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      CollageType.FOUR_IMAGE_4 -> CollageView4Image4(
        context,
        attrs,
        layoutWidth,
        layoutHeight,
        isBorderEnabled,
        imageUris
      )
      else -> {
        Log.d(TAG, "getCollage: invalid collage ID")
        getCollage(CollageType.TWO_IMAGE_VERTICAL)
      }
    }
  }

  enum class CollageType {
    TWO_IMAGE_VERTICAL,
    TWO_IMAGE_HORIZONTAL,
    THREE_IMAGE_0,
    THREE_IMAGE_1,
    THREE_IMAGE_2,
    THREE_IMAGE_3,
    THREE_IMAGE_VERTICAL,
    THREE_IMAGE_HORIZONTAL,
    FOUR_IMAGE_0,
    FOUR_IMAGE_1,
    FOUR_IMAGE_2,
    FOUR_IMAGE_3,
    FOUR_IMAGE_4
  }

  companion object {
    private const val TAG = "CollageViewFactory"
  }
}