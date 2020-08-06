package com.gerardbradshaw.mixup.ui.moreapps

import androidx.lifecycle.ViewModel
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.di.moreapps.DaggerMoreAppsComponent
import com.gerardbradshaw.mixup.models.AppInfo

class MoreAppsViewModel : ViewModel() {

  private val appList = ArrayList<AppInfo>()

  init {
    val component = DaggerMoreAppsComponent
      .builder()
      .titleRes(R.string.mater_title)
      .descriptionRes(R.string.mater_description)
      .urlRes(R.string.mater_url)
      .iconRes(R.drawable.img_mater_logo)
      .build()

    val materAppInfo = component.appInfo

    appList.add(materAppInfo)
  }

  fun getAppList() : ArrayList<AppInfo> {
    return appList
  }
}