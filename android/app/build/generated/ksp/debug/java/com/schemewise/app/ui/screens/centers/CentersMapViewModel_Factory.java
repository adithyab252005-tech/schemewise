package com.schemewise.app.ui.screens.centers;

import com.schemewise.app.data.local.PrefsManager;
import com.schemewise.app.data.repository.ProfileRepository;
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
public final class CentersMapViewModel_Factory implements Factory<CentersMapViewModel> {
  private final Provider<ProfileRepository> profileRepoProvider;

  private final Provider<PrefsManager> prefsProvider;

  public CentersMapViewModel_Factory(Provider<ProfileRepository> profileRepoProvider,
      Provider<PrefsManager> prefsProvider) {
    this.profileRepoProvider = profileRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public CentersMapViewModel get() {
    return newInstance(profileRepoProvider.get(), prefsProvider.get());
  }

  public static CentersMapViewModel_Factory create(Provider<ProfileRepository> profileRepoProvider,
      Provider<PrefsManager> prefsProvider) {
    return new CentersMapViewModel_Factory(profileRepoProvider, prefsProvider);
  }

  public static CentersMapViewModel newInstance(ProfileRepository profileRepo, PrefsManager prefs) {
    return new CentersMapViewModel(profileRepo, prefs);
  }
}
