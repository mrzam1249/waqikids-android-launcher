package com.waqikids.launcher.ui.parentmode

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waqikids.launcher.data.api.WaqiApi
import com.waqikids.launcher.data.api.dto.PinVerifyRequest
import com.waqikids.launcher.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

private const val TAG = "ParentModeViewModel"

data class ParentModeUiState(
    val enteredPin: String = "",
    val isLoading: Boolean = false,
    val isUnlocked: Boolean = false,
    val isLocked: Boolean = false, // Rate limited
    val error: String? = null,
    val attemptsRemaining: Int? = null
)

@HiltViewModel
class ParentModeViewModel @Inject constructor(
    private val api: WaqiApi,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ParentModeUiState())
    val uiState: StateFlow<ParentModeUiState> = _uiState.asStateFlow()
    
    fun appendDigit(digit: String) {
        val currentPin = _uiState.value.enteredPin
        if (currentPin.length < 6) {
            val newPin = currentPin + digit
            _uiState.update { it.copy(enteredPin = newPin, error = null) }
            
            // Auto-verify when 6 digits entered
            if (newPin.length == 6) {
                verifyPin(newPin)
            }
        }
    }
    
    fun deleteLastDigit() {
        val currentPin = _uiState.value.enteredPin
        if (currentPin.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = currentPin.dropLast(1), error = null) }
        }
    }
    
    fun clearPin() {
        _uiState.update { it.copy(enteredPin = "", error = null) }
    }
    
    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val deviceId = preferencesManager.getDeviceId()
            if (deviceId == null) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Device not registered",
                        enteredPin = ""
                    ) 
                }
                return@launch
            }
            
            // First try local verification with cached hash
            val cachedHash = preferencesManager.getCachedPinHash()
            if (cachedHash != null) {
                try {
                    if (BCrypt.checkpw(pin, cachedHash)) {
                        Log.d(TAG, "PIN verified locally (cached)")
                        onUnlockSuccess()
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Local PIN check failed, trying server", e)
                }
            }
            
            // Fall back to server verification
            try {
                val response = api.verifyParentPin(
                    PinVerifyRequest(
                        childDeviceId = deviceId,
                        pin = pin
                    )
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.valid == true) {
                        // Cache the PIN hash for offline use
                        body.pinHash?.let { hash ->
                            preferencesManager.setCachedPinHash(hash)
                        }
                        onUnlockSuccess()
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = body?.error ?: "Incorrect PIN",
                                enteredPin = "",
                                attemptsRemaining = body?.attemptsRemaining
                            ) 
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "PIN verify failed: $errorBody")
                    
                    // Check for rate limiting
                    if (response.code() == 429) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Too many attempts. Try again later.",
                                isLocked = true,
                                enteredPin = ""
                            ) 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Verification failed",
                                enteredPin = ""
                            ) 
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "PIN verify error", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Connection error. Check internet.",
                        enteredPin = ""
                    ) 
                }
            }
        }
    }
    
    private fun onUnlockSuccess() {
        // Set parent mode expiry (10 minutes from now)
        val expiryTime = System.currentTimeMillis() + (10 * 60 * 1000)
        preferencesManager.setParentModeExpiry(expiryTime)
        
        _uiState.update { 
            it.copy(
                isLoading = false, 
                isUnlocked = true,
                error = null
            ) 
        }
    }
}
