package com.gerardbradshaw.mixup.di.moreapps

import com.gerardbradshaw.mixup.models.AppInfo
import com.gerardbradshaw.mixup.ui.moreapps.MoreAppsFragment
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent
interface MoreAppsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): MoreAppsComponent
  }

  @Named("mater_info")
  fun provideAppInfo(): AppInfo

  fun inject(fragment: MoreAppsFragment)
}