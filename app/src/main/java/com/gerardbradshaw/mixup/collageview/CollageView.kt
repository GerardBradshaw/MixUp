package com.gerardbradshaw.mixup.collageview

import android.net.Uri

interface CollageView {

  fun imageCount(): Int

  fun setImageAt(index: Int, uri: Uri?)
}