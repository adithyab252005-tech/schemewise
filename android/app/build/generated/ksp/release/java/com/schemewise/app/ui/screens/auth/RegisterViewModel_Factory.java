package com.schemewise.app.ui.screens.auth;

import com.schemewise.app.data.repository.AuthRepository;
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
public final class RegisterViewModel_Factory implements Factory<RegisterViewModel> {
  private final Provider<AuthRepository> authRepoProvider;

  public RegisterViewModel_Factory(Provider<AuthRepository> authRepoProvider) {
    this.authRepoProvider = authRepoProvider;
  }

  @Override
  public RegisterViewModel get() {
    return newInstance(authRepoProvider.get());
  }

  public static RegisterViewModel_Factory create(Provider<AuthRepository> authRepoProvider) {
    return new RegisterViewModel_Factory(authRepoProvider);
  }

  public static RegisterViewModel newInstance(AuthRepository authRepo) {
    return new RegisterViewModel(authRepo);
  }
}
