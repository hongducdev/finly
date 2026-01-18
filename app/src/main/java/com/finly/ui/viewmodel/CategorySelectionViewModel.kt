package com.finly.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.finly.data.local.entity.TransactionType
import com.finly.data.repository.CustomCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel cho Category Selection Screen
 */
@HiltViewModel
class CategorySelectionViewModel @Inject constructor(
    private val customCategoryRepository: CustomCategoryRepository
) : ViewModel() {
    
    fun getCustomCategoriesByType(type: TransactionType) =
        customCategoryRepository.getCustomCategoriesByType(type)
    
    suspend fun deleteCustomCategory(category: com.finly.data.local.entity.CustomCategory) {
        customCategoryRepository.deleteCustomCategory(category)
    }
}
