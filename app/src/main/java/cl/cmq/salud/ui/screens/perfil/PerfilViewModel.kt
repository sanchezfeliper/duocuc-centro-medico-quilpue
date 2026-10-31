package cl.cmq.salud.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel del destino Perfil.
 *
 * RF-30: carga los datos del usuario autenticado desde Room (repository).
 * RF-31: lista los documentos subidos por ese usuario, ordenados por
 *        fechaCarga descendente (tipo, estado, fecha).
 */
class PerfilViewModel(
    private val sessionRut: String,
    private val repo: DocumentRepository = AppContainer.documentRepository
) : ViewModel() {

    companion object {
        fun factory(sessionRut: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PerfilViewModel(sessionRut) as T
            }
    }

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val usuario = repo.buscarFuncionario(sessionRut)
                val documentos = repo.buscarPorCargador(sessionRut)
                _uiState.value = PerfilUiState(
                    rut = usuario?.rut ?: sessionRut,
                    nombreCompleto = usuario?.nombreCompleto ?: "",
                    cargo = usuario?.cargo ?: "",
                    centroSalud = usuario?.centroSalud ?: "",
                    correo = usuario?.correo ?: "",
                    perfilRol = usuario?.perfilRol ?: "",
                    documentos = documentos,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudieron cargar los datos del perfil: ${e.message}"
                )
            }
        }
    }
}
