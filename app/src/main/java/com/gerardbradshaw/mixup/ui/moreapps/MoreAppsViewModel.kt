package com.gerardbradshaw.mixup.ui.moreapps

import androidx.lifecycle.ViewModel
import com.gerardbradshaw.mixup.models.AppInfo
import com.gerardbradshaw.mixup.di.moreapps.DaggerMoreAppsComponent

class MoreAppsViewModel : ViewModel() {

  private val appList = ArrayList<AppInfo>()

  init {
    val component = DaggerMoreAppsComponent.create()

    val materAppInfo = component.appInfo

    appList.add(materAppInfo)
  }

  fun getAppList() : ArrayList<AppInfo> {
    return appList
  }
}