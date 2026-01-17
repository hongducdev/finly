package com.finly.service;

import com.finly.data.repository.TransactionRepository;
import com.finly.parser.ParserFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class TransactionNotificationService_MembersInjector implements MembersInjector<TransactionNotificationService> {
  private final Provider<ParserFactory> parserFactoryProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public TransactionNotificationService_MembersInjector(
      Provider<ParserFactory> parserFactoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.parserFactoryProvider = parserFactoryProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  public static MembersInjector<TransactionNotificationService> create(
      Provider<ParserFactory> parserFactoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new TransactionNotificationService_MembersInjector(parserFactoryProvider, transactionRepositoryProvider);
  }

  @Override
  public void injectMembers(TransactionNotificationService instance) {
    injectParserFactory(instance, parserFactoryProvider.get());
    injectTransactionRepository(instance, transactionRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.finly.service.TransactionNotificationService.parserFactory")
  public static void injectParserFactory(TransactionNotificationService instance,
      ParserFactory parserFactory) {
    instance.parserFactory = parserFactory;
  }

  @InjectedFieldSignature("com.finly.service.TransactionNotificationService.transactionRepository")
  public static void injectTransactionRepository(TransactionNotificationService instance,
      TransactionRepository transactionRepository) {
    instance.transactionRepository = transactionRepository;
  }
}
