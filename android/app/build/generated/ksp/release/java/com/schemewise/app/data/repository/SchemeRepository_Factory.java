package com.schemewise.app.data.repository;

import com.schemewise.app.data.api.ApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SchemeRepository_Factory implements Factory<SchemeRepository> {
  private final Provider<ApiService> apiProvider;

  public SchemeRepository_Factory(Provider<ApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public SchemeRepository get() {
    return newInstance(apiProvider.get());
  }

  public static SchemeRepository_Factory create(Provider<ApiService> apiProvider) {
    return new SchemeRepository_Factory(apiProvider);
  }

  public static SchemeRepository newInstance(ApiService api) {
    return new SchemeRepository(api);
  }
}
