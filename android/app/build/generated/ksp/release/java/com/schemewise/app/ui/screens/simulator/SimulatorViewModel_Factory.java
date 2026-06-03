package com.schemewise.app.ui.screens.simulator;

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
public final class SimulatorViewModel_Factory implements Factory<SimulatorViewModel> {
  private final Provider<SchemeRepository> schemeRepoProvider;

  public SimulatorViewModel_Factory(Provider<SchemeRepository> schemeRepoProvider) {
    this.schemeRepoProvider = schemeRepoProvider;
  }

  @Override
  public SimulatorViewModel get() {
    return newInstance(schemeRepoProvider.get());
  }

  public static SimulatorViewModel_Factory create(Provider<SchemeRepository> schemeRepoProvider) {
    return new SimulatorViewModel_Factory(schemeRepoProvider);
  }

  public static SimulatorViewModel newInstance(SchemeRepository schemeRepo) {
    return new SimulatorViewModel(schemeRepo);
  }
}
