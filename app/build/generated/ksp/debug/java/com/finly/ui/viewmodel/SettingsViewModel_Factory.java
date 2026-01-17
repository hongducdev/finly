package com.finly.ui.viewmodel;

import android.content.Context;
import com.finly.data.repository.TransactionRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
    "KotlinInternalInJava"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<Context> contextProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(transactionRepositoryProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(TransactionRepository transactionRepository,
      Context context) {
    return new SettingsViewModel(transactionRepository, context);
  }
}
