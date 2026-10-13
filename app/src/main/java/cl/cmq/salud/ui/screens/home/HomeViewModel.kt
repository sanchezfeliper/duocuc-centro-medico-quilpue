package cl.cmq.salud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: DocumentRepository = AppContainer.documentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { cargarResumen() }

    /** Carga resumen del usuario. Identidad ficticia (RN-03); el contador de pendientes sale del repositorio compartido. */
    private fun cargarResumen() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(
                rut = "12.345.678-9",
                nombreUsuario = "Maria Gonzalez",
                perfil = "Personal Administrativo",
                centroSalud = "CESFAM Quilpue",
                validacionesPendientes = repo.listarPendientes().size,
                documentosPorVencer = 2
            )
        }
    }

    /** Refresca el contador al volver a Home (ej: tras decidir validaciones en P06). */
    fun refrescarContador() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(validacionesPendientes = repo.listarPendientes().size)
        }
    }

    fun cerrarSesion() {
        _uiState.value = _uiState.value.copy(isSessionActive = false)
    }
}
