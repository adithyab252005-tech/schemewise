package com.schemewise.app.ui.screens.scheme;

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
public final class CompareViewModel_Factory implements Factory<CompareViewModel> {
  private final Provider<SchemeRepository> schemeRepoProvider;

  private final Provider<ProfileRepository> profileRepoProvider;

  public CompareViewModel_Factory(Provider<SchemeRepository> schemeRepoProvider,
      Provider<ProfileRepository> profileRepoProvider) {
    this.schemeRepoProvider = schemeRepoProvider;
    this.profileRepoProvider = profileRepoProvider;
  }

  @Override
  public CompareViewModel get() {
    return newInstance(schemeRepoProvider.get(), profileRepoProvider.get());
  }

  public static CompareViewModel_Factory create(Provider<SchemeRepository> schemeRepoProvider,
      Provider<ProfileRepository> profileRepoProvider) {
    return new CompareViewModel_Factory(schemeRepoProvider, profileRepoProvider);
  }

  public static CompareViewModel newInstance(SchemeRepository schemeRepo,
      ProfileRepository profileRepo) {
    return new CompareViewModel(schemeRepo, profileRepo);
  }
}
