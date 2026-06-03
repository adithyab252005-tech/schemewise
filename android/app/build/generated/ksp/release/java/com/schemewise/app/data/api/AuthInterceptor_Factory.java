package com.schemewise.app.data.api;

import com.schemewise.app.data.local.PrefsManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class AuthInterceptor_Factory implements Factory<AuthInterceptor> {
  private final Provider<PrefsManager> prefsProvider;

  public AuthInterceptor_Factory(Provider<PrefsManager> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public AuthInterceptor get() {
    return newInstance(prefsProvider.get());
  }

  public static AuthInterceptor_Factory create(Provider<PrefsManager> prefsProvider) {
    return new AuthInterceptor_Factory(prefsProvider);
  }

  public static AuthInterceptor newInstance(PrefsManager prefs) {
    return new AuthInterceptor(prefs);
  }
}
