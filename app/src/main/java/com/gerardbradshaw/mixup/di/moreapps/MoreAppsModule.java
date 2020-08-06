package com.gerardbradshaw.mixup.di.moreapps;

import com.gerardbradshaw.mixup.models.AppInfo;

import dagger.Module;
import dagger.Provides;

@Module
public class MoreAppsModule {
  private int titleRes;

  public MoreAppsModule(int titleRes) {
    this.titleRes = titleRes;
  }

  @Provides
  int provideTitleRes() {
    return titleRes;
  }

/*
  private int titleRes;
  private int descriptionRes;
  private int iconRes;
  private int urlRes;

  public AppInfoModule(int titleRes, int descriptionRes, int iconRes, int urlRes) {
    this.titleRes = titleRes;
    this.descriptionRes = descriptionRes;
    this.iconRes = iconRes;
    this.urlRes = urlRes;
  }

  @Provides
  AppInfo provideAppInfo() {
    return new AppInfo(titleRes, descriptionRes, iconRes, urlRes);
  }

   */
}