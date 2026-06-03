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
public final class ProfileRepository_Factory implements Factory<ProfileRepository> {
  private final Provider<ApiService> apiProvider;

  private final Provider<PrefsManager> prefsProvider;

  public ProfileRepository_Factory(Provider<ApiService> apiProvider,
      Provider<PrefsManager> prefsProvider) {
    this.apiProvider = apiProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public ProfileRepository get() {
    return newInstance(apiProvider.get(), prefsProvider.get());
  }

  public static ProfileRepository_Factory create(Provider<ApiService> apiProvider,
      Provider<PrefsManager> prefsProvider) {
    return new ProfileRepository_Factory(apiProvider, prefsProvider);
  }

  public static ProfileRepository newInstance(ApiService api, PrefsManager prefs) {
    return new ProfileRepository(api, prefs);
  }
}
