package com.gerardbradshaw.mixup.utils

import android.graphics.Color
import kotlin.math.floor
import kotlin.math.roundToInt

class ColorPickerUtil(private val segmentCount: Int) {

  private val spanCount = 7

  fun getIthColor(i: Int): Int {
    val segmentWidth = segmentCount.toFloat() / spanCount.toFloat()
    val spanNumber = floor(i / segmentWidth)
    val progressInSegment = (i - (spanNumber * segmentWidth)) / (segmentWidth)

    val full = 255
    val fadeIn = (255f * progressInSegment).roundToInt()
    val fadeOut = (255f * (1f - progressInSegment)).roundToInt()
    val none = 0

    return when {
      spanNumber < 1 -> Color.argb(full, full, fadeIn, none)
      spanNumber < 2 -> Color.argb(full, fadeOut, full, none)
      spanNumber < 3 -> Color.argb(full, none, full, fadeIn)
      spanNumber < 4 -> Color.argb(full, none, fadeOut, full)
      spanNumber < 5 -> Color.argb(full, fadeIn, none, full)
      spanNumber < 6 -> Color.argb(full, full, none, fadeOut)
      spanNumber <= 7 -> Color.argb(full, fadeIn, fadeIn, fadeIn)
      else -> Color.argb(full, none, none, none)
    }
  }
}