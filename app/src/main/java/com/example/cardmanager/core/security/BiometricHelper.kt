package com.example.cardmanager.core.security

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun showBiometricPromptForAction(
        activity: FragmentActivity,
        title: String = "Confirm Deletion",
        subtitle: String = "Authenticate to delete this card",
        onError: ((errorCode: Int, errString: String) -> Unit)? = null,
        onSuccess: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError?.invoke(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    // Biometric sensor will keep listening or prompt user for retry
                }
            }
        )

        prompt.authenticate(promptInfo)
    }
}

fun Context.findActivity(): FragmentActivity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}
