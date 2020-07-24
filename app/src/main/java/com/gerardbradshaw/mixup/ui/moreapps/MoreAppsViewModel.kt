package com.gerardbradshaw.mixup.ui.moreapps

import androidx.lifecycle.ViewModel
import com.gerardbradshaw.mixup.AppInfo
import com.gerardbradshaw.mixup.R

class MoreAppsViewModel : ViewModel() {
  private val appList = ArrayList<AppInfo>()

  init {
    appList.add(
      AppInfo(
        R.string.mater_title,
        R.string.mater_description,
        R.drawable.img_mater_logo,
        R.string.mater_url))
  }

  fun getAppList() : ArrayList<AppInfo> {
    return appList
  }
}