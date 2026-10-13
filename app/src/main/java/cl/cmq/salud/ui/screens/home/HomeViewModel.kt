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

    /** Carga resumen del usuario. Identidad ficticia (RN-03); los contadores salen del repositorio compartido. */
    private fun cargarResumen() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(
                rut = "12.345.678-9",
                nombreUsuario = "Maria Gonzalez",
                perfil = "Personal Administrativo",
                centroSalud = "CESFAM Quilpue",
                validacionesPendientes = repo.listarPendientes().size,
                documentosEnAlmacenamiento = repo.contarDocumentos(),
                documentosPorVencer = 2
            )
        }
    }

    /** Refresca los contadores al volver a Home (ej: tras cargar o validar documentos). */
    fun refrescarContador() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                validacionesPendientes = repo.listarPendientes().size,
                documentosEnAlmacenamiento = repo.contarDocumentos()
            )
        }
    }

    fun cerrarSesion() {
        _uiState.value = _uiState.value.copy(isSessionActive = false)
    }
}
