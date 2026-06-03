package com.schemewise.app.ui.screens.onboarding;

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
public final class DetailsFillingViewModel_Factory implements Factory<DetailsFillingViewModel> {
  private final Provider<ProfileRepository> profileRepoProvider;

  private final Provider<SchemeRepository> schemeRepoProvider;

  private final Provider<PrefsManager> prefsProvider;

  public DetailsFillingViewModel_Factory(Provider<ProfileRepository> profileRepoProvider,
      Provider<SchemeRepository> schemeRepoProvider, Provider<PrefsManager> prefsProvider) {
    this.profileRepoProvider = profileRepoProvider;
    this.schemeRepoProvider = schemeRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public DetailsFillingViewModel get() {
    return newInstance(profileRepoProvider.get(), schemeRepoProvider.get(), prefsProvider.get());
  }

  public static DetailsFillingViewModel_Factory create(
      Provider<ProfileRepository> profileRepoProvider,
      Provider<SchemeRepository> schemeRepoProvider, Provider<PrefsManager> prefsProvider) {
    return new DetailsFillingViewModel_Factory(profileRepoProvider, schemeRepoProvider, prefsProvider);
  }

  public static DetailsFillingViewModel newInstance(ProfileRepository profileRepo,
      SchemeRepository schemeRepo, PrefsManager prefs) {
    return new DetailsFillingViewModel(profileRepo, schemeRepo, prefs);
  }
}
