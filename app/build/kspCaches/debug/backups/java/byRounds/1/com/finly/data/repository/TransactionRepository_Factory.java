package com.finly.data.repository;

import com.finly.data.local.TransactionDao;
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
public final class TransactionRepository_Factory implements Factory<TransactionRepository> {
  private final Provider<TransactionDao> transactionDaoProvider;

  public TransactionRepository_Factory(Provider<TransactionDao> transactionDaoProvider) {
    this.transactionDaoProvider = transactionDaoProvider;
  }

  @Override
  public TransactionRepository get() {
    return newInstance(transactionDaoProvider.get());
  }

  public static TransactionRepository_Factory create(
      Provider<TransactionDao> transactionDaoProvider) {
    return new TransactionRepository_Factory(transactionDaoProvider);
  }

  public static TransactionRepository newInstance(TransactionDao transactionDao) {
    return new TransactionRepository(transactionDao);
  }
}
