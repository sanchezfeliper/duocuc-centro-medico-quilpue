package cl.cmq.salud.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onRutChange(value: String) {
        _uiState.update { it.copy(rut = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.rut.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese RUT y contraseña") }
            return
        }
        _uiState.update { it.copy(isAuthenticating = true, errorMessage = null) }
        viewModelScope.launch {
            delay(800) // Simulación de autenticación (datos ficticios)
            _uiState.update {
                it.copy(isAuthenticating = false, isAuthenticated = true)
            }
        }
    }
}