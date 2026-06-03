package com.schemewise.app.di;

import com.schemewise.app.data.api.AuthInterceptor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideOkHttpFactory implements Factory<OkHttpClient> {
  private final Provider<AuthInterceptor> authInterceptorProvider;

  public AppModule_ProvideOkHttpFactory(Provider<AuthInterceptor> authInterceptorProvider) {
    this.authInterceptorProvider = authInterceptorProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttp(authInterceptorProvider.get());
  }

  public static AppModule_ProvideOkHttpFactory create(
      Provider<AuthInterceptor> authInterceptorProvider) {
    return new AppModule_ProvideOkHttpFactory(authInterceptorProvider);
  }

  public static OkHttpClient provideOkHttp(AuthInterceptor authInterceptor) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideOkHttp(authInterceptor));
  }
}
