package com.gerardbradshaw.mixup.models

import javax.inject.Inject

class AppInfo @Inject constructor() {
  val titleRes: Int = com.gerardbradshaw.mixup.R.string.mater_title
  val descriptionRes: Int = com.gerardbradshaw.mixup.R.string.mater_description
  val iconRes: Int = com.gerardbradshaw.mixup.R.drawable.img_mater_logo
  val urlRes: Int = com.gerardbradshaw.mixup.R.string.mater_url
}