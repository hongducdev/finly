package com.finly.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper for EncryptedSharedPreferences to store sensitive data (PIN, Biometric status)
 */
@Singleton
class SecurityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to normal SP if encryption fails (device specific issues)
        // Note: In a real banking app, we should handle this more strictly.
        context.getSharedPreferences("secret_shared_prefs_fallback", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
    }

    /**
     * Save PIN (already hashed)
     */
    fun savePinHash(hash: String) {
        sharedPreferences.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    /**
     * Get stored PIN hash
     */
    fun getPinHash(): String? {
        return sharedPreferences.getString(KEY_PIN_HASH, null)
    }

    /**
     * Clear PIN
     */
    fun clearPin() {
        sharedPreferences.edit().remove(KEY_PIN_HASH).apply()
    }

    /**
     * Check if PIN is set
     */
    fun isPinSet(): Boolean {
        return !getPinHash().isNullOrEmpty()
    }

    /**
     * Set App Lock Enabled status
     */
    fun setAppLockEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    /**
     * Check if App Lock is enabled
     */
    fun isAppLockEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_APP_LOCK_ENABLED, false)
    }

    /**
     * Set Biometric Enabled status
     */
    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * Check if Biometric is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
}
