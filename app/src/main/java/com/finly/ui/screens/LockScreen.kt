package com.finly.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.finly.data.local.SecurityPreferences
import com.finly.util.AppLockManager
import java.security.MessageDigest

/**
 * Màn hình khóa ứng dụng
 */
@Composable
fun LockScreen(
    appLockManager: AppLockManager,
    securityPreferences: SecurityPreferences
) {
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current
    var attempts by remember { mutableIntStateOf(0) }

    // Biometric Logic
    val isBiometricEnabled = securityPreferences.isBiometricEnabled()

    LaunchedEffect(Unit) {
        if (isBiometricEnabled) {
            showBiometricPrompt(context as FragmentActivity, appLockManager)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint, // Temporary icon
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Nhập mã PIN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            // Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                keys.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        row.forEach { key ->
                            KeypadButton(
                                key = key,
                                showBiometric = isBiometricEnabled,
                                onClick = {
                                    handleKeyInput(
                                        key = key,
                                        currentPin = pin,
                                        onPinChange = { newPin -> pin = newPin },
                                        onBiometricClick = {
                                            if (isBiometricEnabled) {
                                                showBiometricPrompt(context as FragmentActivity, appLockManager)
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Check PIN when filled
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            val storedHash = securityPreferences.getPinHash()
            val inputHash = hashPin(pin)
            
            if (storedHash == inputHash) {
                appLockManager.unlockApp()
                pin = ""
            } else {
                attempts++
                pin = ""
                Toast.makeText(context, "Mã PIN không đúng", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    showBiometric: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (key == "BIO" && !showBiometric) Color.Transparent
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable(
                enabled = !(key == "BIO" && !showBiometric),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "DEL" -> Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurface
            )
            "BIO" -> if (showBiometric) Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biometric",
                tint = MaterialTheme.colorScheme.primary
            )
            else -> Text(
                text = key,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun handleKeyInput(
    key: String,
    currentPin: String,
    onPinChange: (String) -> Unit,
    onBiometricClick: () -> Unit
) {
    when (key) {
        "DEL" -> if (currentPin.isNotEmpty()) onPinChange(currentPin.dropLast(1))
        "BIO" -> onBiometricClick()
        else -> if (currentPin.length < 4) onPinChange(currentPin + key)
    }
}

private fun hashPin(pin: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun showBiometricPrompt(activity: FragmentActivity, appLockManager: AppLockManager) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                appLockManager.unlockApp()
            }
            // Optional: Handle error/failure
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Xác thực sinh trắc học")
        .setSubtitle("Sử dụng vân tay hoặc khuôn mặt để mở khóa")
        .setNegativeButtonText("Sử dụng PIN")
        .build()

    biometricPrompt.authenticate(promptInfo)
}
