package com.gerardbradshaw.mixup.di.moreapps;

import com.gerardbradshaw.mixup.models.AppInfo;

import javax.inject.Named;

import dagger.BindsInstance;
import dagger.Component;

@Component//(modules = MoreAppsModule.class)
public interface MoreAppsComponent {

  AppInfo getAppInfo();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder titleRes(@Named("titleRes") int titleRes);

    @BindsInstance
    Builder descriptionRes(@Named("descriptionRes") int descriptionRes);

    @BindsInstance
    Builder iconRes(@Named("iconRes") int iconRes);

    @BindsInstance
    Builder urlRes(@Named("urlRes") int urlRes);

    MoreAppsComponent build();
  }
}
