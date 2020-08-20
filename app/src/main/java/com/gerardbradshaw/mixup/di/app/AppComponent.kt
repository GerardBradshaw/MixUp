package com.gerardbradshaw.mixup.di.app

import android.app.Application
import com.gerardbradshaw.mixup.di.activity.ActivityComponent
import com.gerardbradshaw.mixup.di.editor.EditorComponent
import com.gerardbradshaw.mixup.di.moreapps.AppInfoModule
import com.gerardbradshaw.mixup.di.moreapps.MoreAppsComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
  modules = [GlideModule::class, AppInfoModule::class, AppSubcomponents::class])
interface AppComponent {

  @Component.Factory
  interface Factory {
    fun create(@BindsInstance application: Application): AppComponent
  }

  // Types that can be retrieved from the graph
  fun activityComponent(): ActivityComponent.Factory
  fun editorComponent(): EditorComponent.Factory
  fun moreAppsComponent(): MoreAppsComponent.Factory

}