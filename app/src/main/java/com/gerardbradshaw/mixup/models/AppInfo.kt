package com.gerardbradshaw.mixup.models

import javax.inject.Inject
import javax.inject.Named

class AppInfo @Inject constructor(
  @Named("titleRes") var titleRes: Int,
  @Named("descriptionRes") var descriptionRes: Int,
  @Named("iconRes") var iconRes: Int,
  @Named("urlRes") var urlRes: Int)