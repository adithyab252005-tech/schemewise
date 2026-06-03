package com.schemewise.app.ui.screens.home;

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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<SchemeRepository> schemeRepoProvider;

  private final Provider<ProfileRepository> profileRepoProvider;

  private final Provider<PrefsManager> prefsProvider;

  public HomeViewModel_Factory(Provider<SchemeRepository> schemeRepoProvider,
      Provider<ProfileRepository> profileRepoProvider, Provider<PrefsManager> prefsProvider) {
    this.schemeRepoProvider = schemeRepoProvider;
    this.profileRepoProvider = profileRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(schemeRepoProvider.get(), profileRepoProvider.get(), prefsProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<SchemeRepository> schemeRepoProvider,
      Provider<ProfileRepository> profileRepoProvider, Provider<PrefsManager> prefsProvider) {
    return new HomeViewModel_Factory(schemeRepoProvider, profileRepoProvider, prefsProvider);
  }

  public static HomeViewModel newInstance(SchemeRepository schemeRepo,
      ProfileRepository profileRepo, PrefsManager prefs) {
    return new HomeViewModel(schemeRepo, profileRepo, prefs);
  }
}
