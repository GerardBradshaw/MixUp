package com.gerardbradshaw.mixup.ui.editor

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.gerardbradshaw.mixup.R
import java.util.LinkedHashMap

private const val LOG_TAG = "EditorViewModel"

class EditorViewModel : ViewModel() {
  private val imageUris = arrayOfNulls<Uri>(8)
  private val frameIconIdToLayoutIdMap = LinkedHashMap<Int, Int>()
  private val ratioStringToValueMap = LinkedHashMap<String, Float>()

  init {
    initRatioStringToValueMap()
    initFrameIconIdToLayoutIdMap()
  }

  fun getFrameIconIdToLayoutMap(): LinkedHashMap<Int, Int> {
    return frameIconIdToLayoutIdMap
  }

  fun getRatioStringToValueMap(): LinkedHashMap<String, Float> {
    return ratioStringToValueMap
  }

  fun getImageUris(): Array<Uri?> {
    return imageUris
  }

  fun addImageUri(uri: Uri, position: Int) {
    if (position >= imageUris.size) Log.d(LOG_TAG, "Cannot save URI. Invalid position.")
    else imageUris[position] = uri
  }

  private fun initFrameIconIdToLayoutIdMap() {
    frameIconIdToLayoutIdMap[R.drawable.frame_2img_0] = R.layout.frame_2img_0
    frameIconIdToLayoutIdMap[R.drawable.frame_2img_1] = R.layout.frame_2img_1
    frameIconIdToLayoutIdMap[R.drawable.frame_3img_0] = R.layout.frame_3img_0
    frameIconIdToLayoutIdMap[R.drawable.frame_3img_1] = R.layout.frame_3img_1
    frameIconIdToLayoutIdMap[R.drawable.frame_3img_2] = R.layout.frame_3img_2
    frameIconIdToLayoutIdMap[R.drawable.frame_3img_3] = R.layout.frame_3img_3
    frameIconIdToLayoutIdMap[R.drawable.frame_3img_4] = R.layout.frame_3img_4
    frameIconIdToLayoutIdMap[R.drawable.frame_3img_5] = R.layout.frame_3img_5
    frameIconIdToLayoutIdMap[R.drawable.frame_4img_0] = R.layout.frame_4img_0
    frameIconIdToLayoutIdMap[R.drawable.frame_4img_1] = R.layout.frame_4img_1
    frameIconIdToLayoutIdMap[R.drawable.frame_4img_2] = R.layout.frame_4img_2
    frameIconIdToLayoutIdMap[R.drawable.frame_4img_3] = R.layout.frame_4img_3
    frameIconIdToLayoutIdMap[R.drawable.frame_4img_4] = R.layout.frame_4img_4
  }

  private fun initRatioStringToValueMap() {
    ratioStringToValueMap["1:1"] = 1f

    ratioStringToValueMap["16:9"] = 16f / 9f
    ratioStringToValueMap["9:16"] = 9f / 16f

    ratioStringToValueMap["10:8"] = 10f / 8f
    ratioStringToValueMap["8:10"] = 8f / 10f

    ratioStringToValueMap["7:5"] = 7f / 5f
    ratioStringToValueMap["5:7"] = 5f / 7f

    ratioStringToValueMap["4:3"] = 4f / 3f
    ratioStringToValueMap["3:4"] = 3f / 4f

    ratioStringToValueMap["5:3"] = 5f / 3f
    ratioStringToValueMap["3:5"] = 3f / 5f

    ratioStringToValueMap["3:2"] = 3f / 2f
    ratioStringToValueMap["2:3"] = 2f / 3f
  }
}