package com.schemewise.app.ui.screens.onboarding;

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
public final class EligibilityResultsViewModel_Factory implements Factory<EligibilityResultsViewModel> {
  private final Provider<SchemeRepository> schemeRepoProvider;

  private final Provider<ProfileRepository> profileRepoProvider;

  public EligibilityResultsViewModel_Factory(Provider<SchemeRepository> schemeRepoProvider,
      Provider<ProfileRepository> profileRepoProvider) {
    this.schemeRepoProvider = schemeRepoProvider;
    this.profileRepoProvider = profileRepoProvider;
  }

  @Override
  public EligibilityResultsViewModel get() {
    return newInstance(schemeRepoProvider.get(), profileRepoProvider.get());
  }

  public static EligibilityResultsViewModel_Factory create(
      Provider<SchemeRepository> schemeRepoProvider,
      Provider<ProfileRepository> profileRepoProvider) {
    return new EligibilityResultsViewModel_Factory(schemeRepoProvider, profileRepoProvider);
  }

  public static EligibilityResultsViewModel newInstance(SchemeRepository schemeRepo,
      ProfileRepository profileRepo) {
    return new EligibilityResultsViewModel(schemeRepo, profileRepo);
  }
}
