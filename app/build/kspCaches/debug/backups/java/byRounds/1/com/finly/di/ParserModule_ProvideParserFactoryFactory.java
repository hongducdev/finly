package com.finly.di;

import com.finly.parser.ParserFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ParserModule_ProvideParserFactoryFactory implements Factory<ParserFactory> {
  @Override
  public ParserFactory get() {
    return provideParserFactory();
  }

  public static ParserModule_ProvideParserFactoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ParserFactory provideParserFactory() {
    return Preconditions.checkNotNullFromProvides(ParserModule.INSTANCE.provideParserFactory());
  }

  private static final class InstanceHolder {
    private static final ParserModule_ProvideParserFactoryFactory INSTANCE = new ParserModule_ProvideParserFactoryFactory();
  }
}
