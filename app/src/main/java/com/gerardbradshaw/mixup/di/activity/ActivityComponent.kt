package com.gerardbradshaw.mixup.di.activity

import com.gerardbradshaw.mixup.ui.MainActivity
import dagger.Subcomponent

@Subcomponent
interface ActivityComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): ActivityComponent
  }

  fun inject(mainActivity: MainActivity)
}