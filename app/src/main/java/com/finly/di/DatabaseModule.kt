package com.finly.di

import android.content.Context
import androidx.room.Room
import com.finly.data.local.AppDatabase
import com.finly.data.local.BudgetDao
import com.finly.data.local.CustomCategoryDao
import com.finly.data.local.DebtDao
import com.finly.data.local.SavingsGoalDao
import com.finly.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module cho Database
 * Cung cấp Room database và các DAO
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }
    
    @Provides
    @Singleton
    fun provideSavingsGoalDao(database: AppDatabase): SavingsGoalDao {
        return database.savingsGoalDao()
    }
    
    @Provides
    @Singleton
    fun provideCustomCategoryDao(database: AppDatabase): CustomCategoryDao {
        return database.customCategoryDao()
    }
    
    @Provides
    @Singleton
    fun provideDebtDao(database: AppDatabase): DebtDao {
        return database.debtDao()
    }
}
