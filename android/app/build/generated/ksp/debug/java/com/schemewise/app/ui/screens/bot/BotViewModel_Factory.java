package com.schemewise.app.ui.screens.bot;

import com.schemewise.app.data.api.ApiService;
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
public final class BotViewModel_Factory implements Factory<BotViewModel> {
  private final Provider<ApiService> apiProvider;

  private final Provider<ProfileRepository> profileRepoProvider;

  public BotViewModel_Factory(Provider<ApiService> apiProvider,
      Provider<ProfileRepository> profileRepoProvider) {
    this.apiProvider = apiProvider;
    this.profileRepoProvider = profileRepoProvider;
  }

  @Override
  public BotViewModel get() {
    return newInstance(apiProvider.get(), profileRepoProvider.get());
  }

  public static BotViewModel_Factory create(Provider<ApiService> apiProvider,
      Provider<ProfileRepository> profileRepoProvider) {
    return new BotViewModel_Factory(apiProvider, profileRepoProvider);
  }

  public static BotViewModel newInstance(ApiService api, ProfileRepository profileRepo) {
    return new BotViewModel(api, profileRepo);
  }
}
