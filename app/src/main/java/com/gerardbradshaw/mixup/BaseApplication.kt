package com.gerardbradshaw.mixup

import android.app.Application
import com.gerardbradshaw.mixup.di.app.AppComponent
import com.gerardbradshaw.mixup.di.app.DaggerAppComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class BaseApplication : Application() {

  private lateinit var component: AppComponent

  override fun onCreate() {
    super.onCreate()
    component = DaggerAppComponent.builder()
      .setMainDispatcher(Main)
      .setDefaultDispatcher(Default)
      .setIoDispatcher(IO)
      .setApplication(this)
      .build()
  }

  fun getAppComponent(): AppComponent {
    return component
  }

  fun replaceDispatchersForTests() {
    component = DaggerAppComponent.builder()
      .setMainDispatcher(Main)
      .setDefaultDispatcher(Main)
      .setIoDispatcher(Main)
      .setApplication(this)
      .build()
  }
}