package com.gerardbradshaw.mixup.ui.moreapps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gerardbradshaw.mixup.models.AppInfo

class MoreAppsViewModel : ViewModel() {

  private val appList = MutableLiveData<ArrayList<AppInfo>>()

  fun getAppList(): LiveData<ArrayList<AppInfo>> {
    return appList
  }

  fun addAppToList(appInfo: AppInfo) {
    val list = appList.value ?: ArrayList()
    list.add(appInfo)
    appList.value = list
  }
}