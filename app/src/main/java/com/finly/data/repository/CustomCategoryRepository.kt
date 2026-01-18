package com.finly.data.repository

import com.finly.data.local.CustomCategoryDao
import com.finly.data.local.entity.CustomCategory
import com.finly.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository cho Custom Categories
 */
@Singleton
class CustomCategoryRepository @Inject constructor(
    private val customCategoryDao: CustomCategoryDao
) {
    
    fun getAllCustomCategories(): Flow<List<CustomCategory>> {
        return customCategoryDao.getAllCustomCategories()
    }
    
    fun getCustomCategoriesByType(type: TransactionType): Flow<List<CustomCategory>> {
        return customCategoryDao.getCustomCategoriesByType(type)
    }
    
    suspend fun getCustomCategoryById(id: Long): CustomCategory? {
        return customCategoryDao.getCustomCategoryById(id)
    }
    
    suspend fun insertCustomCategory(category: CustomCategory): Long {
        return customCategoryDao.insertCustomCategory(category)
    }
    
    suspend fun updateCustomCategory(category: CustomCategory) {
        customCategoryDao.updateCustomCategory(category)
    }
    
    suspend fun deleteCustomCategory(category: CustomCategory) {
        customCategoryDao.deleteCustomCategory(category)
    }
    
    suspend fun deleteCustomCategoryById(id: Long) {
        customCategoryDao.deleteCustomCategoryById(id)
    }
}
