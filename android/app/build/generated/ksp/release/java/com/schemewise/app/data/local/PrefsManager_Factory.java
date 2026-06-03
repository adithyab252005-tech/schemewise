package com.schemewise.app.data.local;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class PrefsManager_Factory implements Factory<PrefsManager> {
  private final Provider<Context> contextProvider;

  public PrefsManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PrefsManager get() {
    return newInstance(contextProvider.get());
  }

  public static PrefsManager_Factory create(Provider<Context> contextProvider) {
    return new PrefsManager_Factory(contextProvider);
  }

  public static PrefsManager newInstance(Context context) {
    return new PrefsManager(context);
  }
}
