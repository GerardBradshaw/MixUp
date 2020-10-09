package com.gerardbradshaw.v2mixup.di.activity

import com.gerardbradshaw.v2mixup.ui.MainActivity
import dagger.Subcomponent

@Subcomponent
interface ActivityComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): ActivityComponent
  }

  fun inject(mainActivity: MainActivity)
}