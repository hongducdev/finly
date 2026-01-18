package com.finly.data.local

import androidx.room.*
import com.finly.data.local.entity.CustomCategory
import com.finly.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho Custom Categories
 */
@Dao
interface CustomCategoryDao {
    
    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    fun getAllCustomCategories(): Flow<List<CustomCategory>>
    
    @Query("SELECT * FROM custom_categories WHERE type = :type ORDER BY name ASC")
    fun getCustomCategoriesByType(type: TransactionType): Flow<List<CustomCategory>>
    
    @Query("SELECT * FROM custom_categories WHERE id = :id")
    suspend fun getCustomCategoryById(id: Long): CustomCategory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCategory(category: CustomCategory): Long
    
    @Update
    suspend fun updateCustomCategory(category: CustomCategory)
    
    @Delete
    suspend fun deleteCustomCategory(category: CustomCategory)
    
    @Query("DELETE FROM custom_categories WHERE id = :id")
    suspend fun deleteCustomCategoryById(id: Long)
}
