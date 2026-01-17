package com.finly.ui.viewmodel;

import androidx.lifecycle.SavedStateHandle;
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
public final class AddTransactionViewModel_Factory implements Factory<AddTransactionViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AddTransactionViewModel_Factory(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AddTransactionViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static AddTransactionViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AddTransactionViewModel_Factory(transactionRepositoryProvider, savedStateHandleProvider);
  }

  public static AddTransactionViewModel newInstance(TransactionRepository transactionRepository,
      SavedStateHandle savedStateHandle) {
    return new AddTransactionViewModel(transactionRepository, savedStateHandle);
  }
}
