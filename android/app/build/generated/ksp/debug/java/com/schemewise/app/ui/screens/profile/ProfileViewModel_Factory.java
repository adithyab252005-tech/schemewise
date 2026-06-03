package com.schemewise.app.ui.screens.profile;

import com.schemewise.app.data.local.PrefsManager;
import com.schemewise.app.data.repository.AuthRepository;
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<ProfileRepository> profileRepoProvider;

  private final Provider<SchemeRepository> schemeRepoProvider;

  private final Provider<AuthRepository> authRepoProvider;

  private final Provider<PrefsManager> prefsProvider;

  public ProfileViewModel_Factory(Provider<ProfileRepository> profileRepoProvider,
      Provider<SchemeRepository> schemeRepoProvider, Provider<AuthRepository> authRepoProvider,
      Provider<PrefsManager> prefsProvider) {
    this.profileRepoProvider = profileRepoProvider;
    this.schemeRepoProvider = schemeRepoProvider;
    this.authRepoProvider = authRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(profileRepoProvider.get(), schemeRepoProvider.get(), authRepoProvider.get(), prefsProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<ProfileRepository> profileRepoProvider,
      Provider<SchemeRepository> schemeRepoProvider, Provider<AuthRepository> authRepoProvider,
      Provider<PrefsManager> prefsProvider) {
    return new ProfileViewModel_Factory(profileRepoProvider, schemeRepoProvider, authRepoProvider, prefsProvider);
  }

  public static ProfileViewModel newInstance(ProfileRepository profileRepo,
      SchemeRepository schemeRepo, AuthRepository authRepo, PrefsManager prefs) {
    return new ProfileViewModel(profileRepo, schemeRepo, authRepo, prefs);
  }
}
