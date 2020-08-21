package com.gerardbradshaw.mixup.di.moreapps;

import com.gerardbradshaw.mixup.R;
import com.gerardbradshaw.mixup.models.AppInfo;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public abstract class AppInfoModule {

  /*
  I've chosen to use a Set for this. Here's a summary of the methods I could have used:

    Straight-up provides:
     - The annotations below would be @Provides, @Named("mater_info")
     - Then when injecting, I'd call @Inject @Named("mater_info") lateinit var materInfo: AppInfo

    Set:
     - The annotations below would be @IntoSet, @Provides
     - Then when injecting, I'd call @Inject lateinit var appSet: Set<AppInfo>
     - When I want to access Mater's AppInfo, I'd find it somewhere in appSet

    Map:
     - I'd need an @AppKey annotation which would look like this:
            @Documented
            @Target(ElementType.METHOD)
            @Retention(RetentionPolicy.RUNTIME)
            @MapKey
            public @interface AppKey {
              Apps value();
            }
     - I'd need an enum class Apps which contains MATER like this:
            enum class Apps {
              MATER
            }
     - The annotations for provideMaterAppInfo below be @IntoMap, @AppKey(Apps.Mater), @Provides
     - Then when injecting, I'd have to call @Inject lateinit var appMap: Map<App, AppInfo>
     - When I want to access Mater's AppInfo, I'd call appMap.getApps.MATER)
   */

  @IntoSet
  @Provides
  static AppInfo provideMaterAppInfo() {
    return new AppInfo(
        R.string.mater_title,
        R.string.mater_description,
        R.drawable.mater_logo,
        R.string.mater_url);
  }
}