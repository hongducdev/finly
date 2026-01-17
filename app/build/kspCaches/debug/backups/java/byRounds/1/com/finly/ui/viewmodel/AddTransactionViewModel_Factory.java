package com.finly.ui.viewmodel;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
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
public final class AddTransactionViewModel_Factory implements Factory<AddTransactionViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<Context> contextProvider;

  public AddTransactionViewModel_Factory(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider, Provider<Context> contextProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public AddTransactionViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), savedStateHandleProvider.get(), contextProvider.get());
  }

  public static AddTransactionViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider, Provider<Context> contextProvider) {
    return new AddTransactionViewModel_Factory(transactionRepositoryProvider, savedStateHandleProvider, contextProvider);
  }

  public static AddTransactionViewModel newInstance(TransactionRepository transactionRepository,
      SavedStateHandle savedStateHandle, Context context) {
    return new AddTransactionViewModel(transactionRepository, savedStateHandle, context);
  }
}
