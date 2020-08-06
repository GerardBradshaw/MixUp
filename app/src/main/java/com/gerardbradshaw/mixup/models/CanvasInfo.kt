package com.gerardbradshaw.mixup.models

import android.net.Uri
import javax.inject.Inject
import javax.inject.Named

class CanvasInfo constructor(
  @Named("canvasHeight") var height: Float = 0f,
  @Named("canvasWidth") var width: Float = 0f,
  @Named("ratio") var ratio: Float = 1f,
  @Named("imageUris") var imageUris: Array<Uri?> = arrayOfNulls(8)
)