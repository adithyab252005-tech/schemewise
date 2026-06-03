package com.schemewise.app.ui.screens.saved;

import com.schemewise.app.data.local.PrefsManager;
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
public final class SavedViewModel_Factory implements Factory<SavedViewModel> {
  private final Provider<SchemeRepository> schemeRepoProvider;

  private final Provider<PrefsManager> prefsProvider;

  public SavedViewModel_Factory(Provider<SchemeRepository> schemeRepoProvider,
      Provider<PrefsManager> prefsProvider) {
    this.schemeRepoProvider = schemeRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SavedViewModel get() {
    return newInstance(schemeRepoProvider.get(), prefsProvider.get());
  }

  public static SavedViewModel_Factory create(Provider<SchemeRepository> schemeRepoProvider,
      Provider<PrefsManager> prefsProvider) {
    return new SavedViewModel_Factory(schemeRepoProvider, prefsProvider);
  }

  public static SavedViewModel newInstance(SchemeRepository schemeRepo, PrefsManager prefs) {
    return new SavedViewModel(schemeRepo, prefs);
  }
}
