package com.finly.di

import com.finly.parser.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module cho Parser
 * Cung cấp ParserFactory và các parser
 */
@Module
@InstallIn(SingletonComponent::class)
object ParserModule {
    
    @Provides
    @Singleton
    fun provideParserFactory(): ParserFactory {
        return ParserFactory()
    }
}
