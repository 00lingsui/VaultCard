package com.example.cardmanager.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BiometricStatus {
    data object Ready : BiometricStatus
    data object NotEnrolled : BiometricStatus
    data object NotAvailable : BiometricStatus
}

sealed interface BiometricAuthResult {
    data object Success : BiometricAuthResult
    data class Error(val errorCode: Int, val message: String) : BiometricAuthResult
    data object Failed : BiometricAuthResult
}

interface BiometricAuthManager {
    fun checkBiometricAvailability(): BiometricStatus
    fun promptAuthentication(
        activity: FragmentActivity,
        title: String = "Unlock VaultCard",
        subtitle: String = "Authenticate to view sensitive card details",
        onResult: (BiometricAuthResult) -> Unit
    )
}

@Singleton
class BiometricAuthManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BiometricAuthManager {

    private val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

    override fun checkBiometricAvailability(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Ready
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NotEnrolled
            else -> BiometricStatus.NotAvailable
        }
    }

    override fun promptAuthentication(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onResult: (BiometricAuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(BiometricAuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(BiometricAuthResult.Error(errorCode, errString.toString()))
                }

                override fun onAuthenticationFailed() {
                    onResult(BiometricAuthResult.Failed)
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
