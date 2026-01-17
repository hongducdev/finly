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
public final class CalendarViewModel_Factory implements Factory<CalendarViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public CalendarViewModel_Factory(Provider<TransactionRepository> transactionRepositoryProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public CalendarViewModel get() {
    return newInstance(transactionRepositoryProvider.get());
  }

  public static CalendarViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new CalendarViewModel_Factory(transactionRepositoryProvider);
  }

  public static CalendarViewModel newInstance(TransactionRepository transactionRepository) {
    return new CalendarViewModel(transactionRepository);
  }
}
