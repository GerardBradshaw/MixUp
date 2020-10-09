package com.gerardbradshaw.v2mixup.di.app

import com.gerardbradshaw.v2mixup.di.activity.ActivityComponent
import com.gerardbradshaw.v2mixup.di.editor.EditorComponent
import com.gerardbradshaw.v2mixup.di.moreapps.MoreAppsComponent
import dagger.Module

@Module(subcomponents = [ActivityComponent::class, EditorComponent::class, MoreAppsComponent::class])
interface AppSubcomponents