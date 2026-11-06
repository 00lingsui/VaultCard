package com.example.cardmanager.core.security

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val canAuthenticate: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState = _authState.asStateFlow()

    fun authenticate(activity: FragmentActivity) {
        val status = biometricAuthManager.checkBiometricAvailability()

        if (status is BiometricStatus.NotAvailable) {
            _authState.value = AuthUiState(isAuthenticated = true, canAuthenticate = false)
            return
        }

        biometricAuthManager.promptAuthentication(activity) { result ->
            when (result) {
                is BiometricAuthResult.Success -> {
                    _authState.value = AuthUiState(isAuthenticated = true)
                }
                is BiometricAuthResult.Error -> {
                    _authState.value = _authState.value.copy(errorMessage = result.message)
                }
                is BiometricAuthResult.Failed -> {
                    _authState.value = _authState.value.copy(errorMessage = "Biometric check failed")
                }
            }
        }
    }
}
