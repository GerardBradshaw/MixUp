package com.gerardbradshaw.mixup.di.app;

import com.gerardbradshaw.collageview.util.ImageUtil;

import javax.inject.Named;

import dagger.BindsInstance;
import dagger.Module;
import dagger.Provides;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.Dispatchers;

@Module
public abstract class ImageUtilModule {

  @Provides
  static ImageUtil provideImageUtil(
      @Named("thread_main") CoroutineDispatcher mainDispatcher,
      @Named("thread_default") CoroutineDispatcher defaultDispatcher,
      @Named("thread_io") CoroutineDispatcher ioDispatcher
  ) {
    return new ImageUtil(mainDispatcher, defaultDispatcher, ioDispatcher);
  }
}
