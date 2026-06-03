package com.schemewise.app.di;

import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class AppModule_ProvideRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> okHttpProvider;

  private final Provider<Moshi> moshiProvider;

  public AppModule_ProvideRetrofitFactory(Provider<OkHttpClient> okHttpProvider,
      Provider<Moshi> moshiProvider) {
    this.okHttpProvider = okHttpProvider;
    this.moshiProvider = moshiProvider;
  }

  @Override
  public Retrofit get() {
    return provideRetrofit(okHttpProvider.get(), moshiProvider.get());
  }

  public static AppModule_ProvideRetrofitFactory create(Provider<OkHttpClient> okHttpProvider,
      Provider<Moshi> moshiProvider) {
    return new AppModule_ProvideRetrofitFactory(okHttpProvider, moshiProvider);
  }

  public static Retrofit provideRetrofit(OkHttpClient okHttp, Moshi moshi) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRetrofit(okHttp, moshi));
  }
}
