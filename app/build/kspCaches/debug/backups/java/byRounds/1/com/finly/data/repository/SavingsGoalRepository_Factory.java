package com.finly.data.repository;

import com.finly.data.local.SavingsGoalDao;
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
    "KotlinInternalInJava"
})
public final class SavingsGoalRepository_Factory implements Factory<SavingsGoalRepository> {
  private final Provider<SavingsGoalDao> savingsGoalDaoProvider;

  public SavingsGoalRepository_Factory(Provider<SavingsGoalDao> savingsGoalDaoProvider) {
    this.savingsGoalDaoProvider = savingsGoalDaoProvider;
  }

  @Override
  public SavingsGoalRepository get() {
    return newInstance(savingsGoalDaoProvider.get());
  }

  public static SavingsGoalRepository_Factory create(
      Provider<SavingsGoalDao> savingsGoalDaoProvider) {
    return new SavingsGoalRepository_Factory(savingsGoalDaoProvider);
  }

  public static SavingsGoalRepository newInstance(SavingsGoalDao savingsGoalDao) {
    return new SavingsGoalRepository(savingsGoalDao);
  }
}
