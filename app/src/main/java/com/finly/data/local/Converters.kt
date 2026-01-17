package com.finly.data.local

import androidx.room.TypeConverter
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType

/**
 * Type converters cho Room
 * Chuyển đổi enum sang String và ngược lại
 */
class Converters {
    
    @TypeConverter
    fun fromTransactionSource(source: TransactionSource): String {
        return source.name
    }
    
    @TypeConverter
    fun toTransactionSource(name: String): TransactionSource {
        return try {
            TransactionSource.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TransactionSource.UNKNOWN
        }
    }
    
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }
    
    @TypeConverter
    fun toTransactionType(name: String): TransactionType {
        return try {
            TransactionType.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TransactionType.EXPENSE
        }
    }
    
    @TypeConverter
    fun fromTransactionCategory(category: TransactionCategory?): String? {
        return category?.name
    }
    
    @TypeConverter
    fun toTransactionCategory(name: String?): TransactionCategory? {
        return name?.let {
            try {
                TransactionCategory.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

