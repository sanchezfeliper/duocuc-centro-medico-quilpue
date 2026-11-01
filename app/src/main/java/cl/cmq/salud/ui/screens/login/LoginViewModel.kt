package cl.cmq.salud.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.domain.session.SesionActual
import cl.cmq.salud.domain.validation.CatalogoCuentasDemo
import cl.cmq.salud.domain.validation.ValidadorRut
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Autenticación simulada del MVP (supuesto S-01).
 *
 * En vez de marcar la sesión como abierta sin más, resuelve las credenciales
 * contra [CatalogoCuentasDemo] (cuentas ficticias, RN-03) y, si coinciden,
 * deja la sesión en [AppContainer] para que el resto de la app sepa quién
 * entró y con qué perfil.
 *
 * Las dependencias se inyectan por constructor para poder probar el flujo sin
 * tocar el singleton global.
 */
class LoginViewModel(
    private val catalogo: CatalogoCuentasDemo = CatalogoCuentasDemo,
    private val abrirSesion: (SesionActual) -> Unit = AppContainer::iniciarSesion
) : ViewModel() {

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
        // Meta 5: la regla de formato vive en el dominio compartido.
        // Solo formato: la autenticación es simulada (S-01) y la identidad
        // real vendrá del backend, por eso aqui no se exige dígito verificador.
        if (!ValidadorRut.esFormatoValido(state.rut)) {
            _uiState.update { it.copy(errorMessage = "Formato de RUT inválido (ej: 18.765.432-7)") }
            return
        }
        _uiState.update { it.copy(isAuthenticating = true, errorMessage = null) }
        viewModelScope.launch {
            delay(800) // Simulación de autenticación (datos ficticios)
            val sesion = catalogo.resolver(state.rut, state.password)
            if (sesion == null) {
                // Formato correcto pero sin cuenta que coincida: no se abre sesión.
                _uiState.update {
                    it.copy(
                        isAuthenticating = false,
                        errorMessage = "Credenciales incorrectas. Verifica tu RUT y contraseña."
                    )
                }
                return@launch
            }
            abrirSesion(sesion)
            _uiState.update {
                it.copy(isAuthenticating = false, isAuthenticated = true)
            }
        }
    }
}
