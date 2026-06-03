package com.schemewise.app.data.repository;

import com.schemewise.app.data.api.ApiService;
import com.schemewise.app.data.local.PrefsManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<ApiService> apiProvider;

  private final Provider<PrefsManager> prefsProvider;

  public AuthRepository_Factory(Provider<ApiService> apiProvider,
      Provider<PrefsManager> prefsProvider) {
    this.apiProvider = apiProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(apiProvider.get(), prefsProvider.get());
  }

  public static AuthRepository_Factory create(Provider<ApiService> apiProvider,
      Provider<PrefsManager> prefsProvider) {
    return new AuthRepository_Factory(apiProvider, prefsProvider);
  }

  public static AuthRepository newInstance(ApiService api, PrefsManager prefs) {
    return new AuthRepository(api, prefs);
  }
}
