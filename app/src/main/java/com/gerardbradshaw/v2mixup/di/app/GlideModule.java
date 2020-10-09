package com.gerardbradshaw.v2mixup.di.app;

import android.app.Application;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.gerardbradshaw.v2mixup.R;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class GlideModule {

  @Singleton
  @Provides
  static RequestOptions provideGlideRequestOptions() {
    return RequestOptions
        .placeholderOf(R.drawable.img_tap_to_add_photo)
        .error(R.drawable.img_tap_to_add_photo);
  }

  @Singleton
  @Provides
  static RequestManager provideGlideInstance(
      Application application,
      RequestOptions requestOptions
  ) {
    return Glide
        .with(application)
        .setDefaultRequestOptions(requestOptions);
  }

  @Singleton
  @Provides
  static Drawable provideTapToAddPhotoDrawable(Application application) {
    return ContextCompat
        .getDrawable(application, R.drawable.img_tap_to_add_photo);
  }
}