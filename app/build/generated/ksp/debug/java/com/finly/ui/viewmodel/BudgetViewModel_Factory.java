package com.finly.ui.viewmodel;

import com.finly.data.repository.BudgetRepository;
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
public final class BudgetViewModel_Factory implements Factory<BudgetViewModel> {
  private final Provider<BudgetRepository> budgetRepositoryProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public BudgetViewModel_Factory(Provider<BudgetRepository> budgetRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.budgetRepositoryProvider = budgetRepositoryProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public BudgetViewModel get() {
    return newInstance(budgetRepositoryProvider.get(), transactionRepositoryProvider.get());
  }

  public static BudgetViewModel_Factory create(Provider<BudgetRepository> budgetRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new BudgetViewModel_Factory(budgetRepositoryProvider, transactionRepositoryProvider);
  }

  public static BudgetViewModel newInstance(BudgetRepository budgetRepository,
      TransactionRepository transactionRepository) {
    return new BudgetViewModel(budgetRepository, transactionRepository);
  }
}
