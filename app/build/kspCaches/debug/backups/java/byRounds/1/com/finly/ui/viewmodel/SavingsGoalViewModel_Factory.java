package com.finly.ui.viewmodel;

import com.finly.data.repository.SavingsGoalRepository;
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
public final class SavingsGoalViewModel_Factory implements Factory<SavingsGoalViewModel> {
  private final Provider<SavingsGoalRepository> savingsGoalRepositoryProvider;

  public SavingsGoalViewModel_Factory(
      Provider<SavingsGoalRepository> savingsGoalRepositoryProvider) {
    this.savingsGoalRepositoryProvider = savingsGoalRepositoryProvider;
  }

  @Override
  public SavingsGoalViewModel get() {
    return newInstance(savingsGoalRepositoryProvider.get());
  }

  public static SavingsGoalViewModel_Factory create(
      Provider<SavingsGoalRepository> savingsGoalRepositoryProvider) {
    return new SavingsGoalViewModel_Factory(savingsGoalRepositoryProvider);
  }

  public static SavingsGoalViewModel newInstance(SavingsGoalRepository savingsGoalRepository) {
    return new SavingsGoalViewModel(savingsGoalRepository);
  }
}
