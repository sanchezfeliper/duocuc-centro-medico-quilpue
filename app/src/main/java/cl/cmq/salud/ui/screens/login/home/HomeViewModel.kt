<<<<<<< HEAD
package cl.cmq.salud.ui.screens.home
=======
package cl.cmq.salud.ui.screens.login.home
>>>>>>> b5d54a074f57dca4c623e45002ea0bcc37fd2f8a

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { cargarResumen() }

    /** Carga resumen del usuario. Datos ficticios (RN-03). */
    private fun cargarResumen() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(
                rut = "12.345.678-9",
                nombreUsuario = "Maria Gonzalez",
                perfil = "Personal Administrativo",
                centroSalud = "CESFAM Quilpue",
                validacionesPendientes = 3,
                documentosPorVencer = 2
            )
        }
    }

    fun cerrarSesion() {
        _uiState.value = _uiState.value.copy(isSessionActive = false)
    }
}
