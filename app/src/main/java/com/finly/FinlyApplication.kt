package com.finly

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class cho Finly
 * Khởi tạo Hilt dependency injection
 */
@HiltAndroidApp
class FinlyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Hilt sẽ tự động khởi tạo các dependency
    }
}
