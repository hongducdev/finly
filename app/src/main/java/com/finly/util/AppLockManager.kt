package com.finly.util

import com.finly.data.local.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton to manage App Lock state (Locked/Unlocked) and session timeout
 */
@Singleton
class AppLockManager @Inject constructor(
    private val securityPreferences: SecurityPreferences
) {

    // Lock Application State
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastActiveTimestamp: Long = 0L
    
    // Timeout in milliseconds (e.g. 10 seconds background grace period)
    // Reduce for testing if needed
    private val LOCK_TIMEOUT_MS = 10_000L 

    /**
     * Check if app should be locked when returning from background
     */
    fun checkAndLock() {
        if (!securityPreferences.isAppLockEnabled() || !securityPreferences.isPinSet()) {
            _isLocked.update { false }
            return
        }

        val currentTime = System.currentTimeMillis()
        if (lastActiveTimestamp > 0 && (currentTime - lastActiveTimestamp) > LOCK_TIMEOUT_MS) {
            _isLocked.update { true }
        }
    }

    /**
     * Call when user interacts with the app or Activity resumes
     */
    fun updateLastActive() {
        lastActiveTimestamp = System.currentTimeMillis()
    }
    
    /**
     * Call when user leaves the app (onSpace)
     */
    fun onAppBackgrounded() {
         lastActiveTimestamp = System.currentTimeMillis()
    }

    /**
     * Manually lock (e.g. from settings, or first launch)
     */
    fun lockApp() {
        if (securityPreferences.isAppLockEnabled() && securityPreferences.isPinSet()) {
            _isLocked.update { true }
        }
    }

    /**
     * Unlock app (after successful PIN/Biometric)
     */
    fun unlockApp() {
        _isLocked.update { false }
        updateLastActive()
    }
    
    /**
     * Initialize lock state on startup
     */
    fun initOnStartup() {
        if (securityPreferences.isAppLockEnabled() && securityPreferences.isPinSet()) {
            _isLocked.update { true }
        }
    }
}
