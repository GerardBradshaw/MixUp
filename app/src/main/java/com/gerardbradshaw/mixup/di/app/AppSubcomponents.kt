package com.gerardbradshaw.mixup.di.app

import com.gerardbradshaw.mixup.di.activity.ActivityComponent
import com.gerardbradshaw.mixup.di.editor.EditorComponent
import com.gerardbradshaw.mixup.di.moreapps.MoreAppsComponent
import dagger.Module

@Module(subcomponents = [ActivityComponent::class, EditorComponent::class, MoreAppsComponent::class])
interface AppSubcomponents