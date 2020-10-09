package com.gerardbradshaw.v2mixup.di.moreapps

import android.content.Context
import com.gerardbradshaw.v2mixup.ui.moreapps.MoreAppsFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [AppInfoModule::class])
interface MoreAppsComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activityContext: Context): MoreAppsComponent
  }

  fun inject(fragment: MoreAppsFragment)
}