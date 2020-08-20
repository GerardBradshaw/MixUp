package com.gerardbradshaw.mixup

import android.app.Application
import com.gerardbradshaw.mixup.di.app.AppComponent
import com.gerardbradshaw.mixup.di.app.DaggerAppComponent

class BaseApplication : Application() {

  private lateinit var component: AppComponent

  override fun onCreate() {
    super.onCreate()
    component = DaggerAppComponent.factory().create(this)
  }

  fun getAppComponent(): AppComponent {
    return component
  }
}