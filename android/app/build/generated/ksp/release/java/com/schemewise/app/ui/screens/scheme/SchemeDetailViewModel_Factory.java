package com.schemewise.app.ui.screens.scheme;

import com.schemewise.app.data.local.PrefsManager;
import com.schemewise.app.data.repository.ProfileRepository;
import com.schemewise.app.data.repository.SchemeRepository;
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
public final class SchemeDetailViewModel_Factory implements Factory<SchemeDetailViewModel> {
  private final Provider<SchemeRepository> schemeRepoProvider;

  private final Provider<ProfileRepository> profileRepoProvider;

  private final Provider<PrefsManager> prefsProvider;

  public SchemeDetailViewModel_Factory(Provider<SchemeRepository> schemeRepoProvider,
      Provider<ProfileRepository> profileRepoProvider, Provider<PrefsManager> prefsProvider) {
    this.schemeRepoProvider = schemeRepoProvider;
    this.profileRepoProvider = profileRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SchemeDetailViewModel get() {
    return newInstance(schemeRepoProvider.get(), profileRepoProvider.get(), prefsProvider.get());
  }

  public static SchemeDetailViewModel_Factory create(Provider<SchemeRepository> schemeRepoProvider,
      Provider<ProfileRepository> profileRepoProvider, Provider<PrefsManager> prefsProvider) {
    return new SchemeDetailViewModel_Factory(schemeRepoProvider, profileRepoProvider, prefsProvider);
  }

  public static SchemeDetailViewModel newInstance(SchemeRepository schemeRepo,
      ProfileRepository profileRepo, PrefsManager prefs) {
    return new SchemeDetailViewModel(schemeRepo, profileRepo, prefs);
  }
}
