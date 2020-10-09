package com.gerardbradshaw.v2mixup.di.editor

import com.gerardbradshaw.v2mixup.ui.editor.EditorFragment
import dagger.Subcomponent

@Subcomponent
interface EditorComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): EditorComponent
  }

  fun inject(fragment: EditorFragment)
}