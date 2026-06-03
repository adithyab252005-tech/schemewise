package com.schemewise.app.di;

import android.content.Context;
import com.schemewise.app.data.local.PrefsManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvidePrefsManagerFactory implements Factory<PrefsManager> {
  private final Provider<Context> ctxProvider;

  public AppModule_ProvidePrefsManagerFactory(Provider<Context> ctxProvider) {
    this.ctxProvider = ctxProvider;
  }

  @Override
  public PrefsManager get() {
    return providePrefsManager(ctxProvider.get());
  }

  public static AppModule_ProvidePrefsManagerFactory create(Provider<Context> ctxProvider) {
    return new AppModule_ProvidePrefsManagerFactory(ctxProvider);
  }

  public static PrefsManager providePrefsManager(Context ctx) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePrefsManager(ctx));
  }
}
