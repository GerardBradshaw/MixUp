package com.gerardbradshaw.mixup.di.moreapps;

import com.gerardbradshaw.mixup.R;
import com.gerardbradshaw.mixup.models.AppInfo;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class AppInfoModule {

  @Provides @Named("mater_info")
  static AppInfo provideMaterAppInfo() {
    return new AppInfo(
        R.string.mater_title,
        R.string.mater_description,
        R.drawable.mater_logo,
        R.string.mater_url);
  }
}