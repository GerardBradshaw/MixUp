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
      @Named("main_thread") CoroutineDispatcher mainDispatcher,
      @Named("default_thread") CoroutineDispatcher defaultDispatcher,
      @Named("io_thread") CoroutineDispatcher ioDispatcher) {

    return new ImageUtil(mainDispatcher, defaultDispatcher, ioDispatcher);
  }
}
