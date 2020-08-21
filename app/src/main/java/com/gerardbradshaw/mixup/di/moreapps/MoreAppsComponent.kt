package com.gerardbradshaw.mixup.di.moreapps

import android.content.Context
import com.gerardbradshaw.mixup.models.AppInfo
import com.gerardbradshaw.mixup.ui.moreapps.MoreAppsFragment
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent(modules = [AppInfoModule::class])
interface MoreAppsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activityContext: Context): MoreAppsComponent
  }

  //@Named("mater_info")
  //fun provideAppInfo(): AppInfo

  fun inject(fragment: MoreAppsFragment)
}