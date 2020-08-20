package com.gerardbradshaw.mixup.ui.editor

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gerardbradshaw.mixup.R
import java.util.LinkedHashMap

private const val LOG_TAG = "EditorViewModel"

class EditorViewModel : ViewModel() {

  val frameIconIdToLayoutId = LinkedHashMap<Int, Int>()
  val ratioStringToValue = LinkedHashMap<String, Float>()
  val canvasRatio = MutableLiveData<Float>()
  var maxHeight = 0f
  var maxWidth = 0f
  val imageUris = arrayOfNulls<Uri>(8)
  var defaultImageUri: Uri? = null

  init {
    initRatioStringToValueMap()
    initFrameIconIdToLayoutIdMap()
    canvasRatio.value = 1f
  }

  fun addImageUri(uri: Uri, position: Int) {
    if (position >= imageUris.size) Log.d(LOG_TAG, "Cannot save URI. Invalid position.")
    else imageUris[position] = uri
  }

  fun setRatio(ratio: Float) {
    canvasRatio.value = ratio
  }

  private fun initFrameIconIdToLayoutIdMap() {
    frameIconIdToLayoutId[R.drawable.frame_2img_0] = R.layout.frame_2img_0
    frameIconIdToLayoutId[R.drawable.frame_2img_1] = R.layout.frame_2img_1
    frameIconIdToLayoutId[R.drawable.frame_3img_0] = R.layout.frame_3img_0
    frameIconIdToLayoutId[R.drawable.frame_3img_1] = R.layout.frame_3img_1
    frameIconIdToLayoutId[R.drawable.frame_3img_2] = R.layout.frame_3img_2
    frameIconIdToLayoutId[R.drawable.frame_3img_3] = R.layout.frame_3img_3
    frameIconIdToLayoutId[R.drawable.frame_3img_4] = R.layout.frame_3img_4
    frameIconIdToLayoutId[R.drawable.frame_3img_5] = R.layout.frame_3img_5
    frameIconIdToLayoutId[R.drawable.frame_4img_0] = R.layout.frame_4img_0
    frameIconIdToLayoutId[R.drawable.frame_4img_1] = R.layout.frame_4img_1
    frameIconIdToLayoutId[R.drawable.frame_4img_2] = R.layout.frame_4img_2
    frameIconIdToLayoutId[R.drawable.frame_4img_3] = R.layout.frame_4img_3
    frameIconIdToLayoutId[R.drawable.frame_4img_4] = R.layout.frame_4img_4
  }

  private fun initRatioStringToValueMap() {
    ratioStringToValue["1:1"] = 1f

    ratioStringToValue["16:9"] = 16f / 9f
    ratioStringToValue["9:16"] = 9f / 16f

    ratioStringToValue["10:8"] = 10f / 8f
    ratioStringToValue["8:10"] = 8f / 10f

    ratioStringToValue["7:5"] = 7f / 5f
    ratioStringToValue["5:7"] = 5f / 7f

    ratioStringToValue["4:3"] = 4f / 3f
    ratioStringToValue["3:4"] = 3f / 4f

    ratioStringToValue["5:3"] = 5f / 3f
    ratioStringToValue["3:5"] = 3f / 5f

    ratioStringToValue["3:2"] = 3f / 2f
    ratioStringToValue["2:3"] = 2f / 3f
  }
}