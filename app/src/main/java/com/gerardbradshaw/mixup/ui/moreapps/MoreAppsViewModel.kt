package com.gerardbradshaw.mixup.ui.moreapps

import androidx.lifecycle.ViewModel
import com.gerardbradshaw.mixup.models.App
import com.gerardbradshaw.mixup.R

class MoreAppsViewModel : ViewModel() {
  private val appList = ArrayList<App>()

  init {
    appList.add(
      App(
        R.string.mater_title,
        R.string.mater_description,
        R.drawable.img_mater_logo,
        R.string.mater_url
      )
    )
  }

  fun getAppList() : ArrayList<App> {
    return appList
  }
}