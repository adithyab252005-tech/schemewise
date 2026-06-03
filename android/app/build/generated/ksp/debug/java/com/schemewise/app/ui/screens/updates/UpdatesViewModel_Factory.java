package com.schemewise.app.ui.screens.updates;

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
public final class UpdatesViewModel_Factory implements Factory<UpdatesViewModel> {
  private final Provider<SchemeRepository> schemeRepositoryProvider;

  public UpdatesViewModel_Factory(Provider<SchemeRepository> schemeRepositoryProvider) {
    this.schemeRepositoryProvider = schemeRepositoryProvider;
  }

  @Override
  public UpdatesViewModel get() {
    return newInstance(schemeRepositoryProvider.get());
  }

  public static UpdatesViewModel_Factory create(
      Provider<SchemeRepository> schemeRepositoryProvider) {
    return new UpdatesViewModel_Factory(schemeRepositoryProvider);
  }

  public static UpdatesViewModel newInstance(SchemeRepository schemeRepository) {
    return new UpdatesViewModel(schemeRepository);
  }
}
