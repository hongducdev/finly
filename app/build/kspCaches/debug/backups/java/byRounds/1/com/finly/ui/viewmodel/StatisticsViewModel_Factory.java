package com.finly.ui.viewmodel;

import com.finly.data.repository.TransactionRepository;
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
    "KotlinInternalInJava"
})
public final class StatisticsViewModel_Factory implements Factory<StatisticsViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public StatisticsViewModel_Factory(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public StatisticsViewModel get() {
    return newInstance(transactionRepositoryProvider.get());
  }

  public static StatisticsViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new StatisticsViewModel_Factory(transactionRepositoryProvider);
  }

  public static StatisticsViewModel newInstance(TransactionRepository transactionRepository) {
    return new StatisticsViewModel(transactionRepository);
  }
}
