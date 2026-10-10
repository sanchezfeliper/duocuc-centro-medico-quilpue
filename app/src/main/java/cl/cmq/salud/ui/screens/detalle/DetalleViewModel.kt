// ui/screens/detalle/DetalleViewModel.kt
package cl.cmq.salud.ui.screens.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Meta 6: ViewModel del destino P04 Detalle. Recibe el ID del documento
 * como argumento de navegacion y resuelve el dato contra la fuente unica
 * (DocumentRepository), no via objetos transportados entre pantallas.
 */
class DetalleViewModel(
    private val repo: DocumentRepository,
    private val idDocumento: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    fun cargarDetalle() {
        _uiState.update { it.copy(isLoading = true, error = null, accesoRegistrado = false) }
        viewModelScope.launch {
            val documento = repo.buscarPorId(idDocumento)
            if (documento == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se encontró el documento #$idDocumento. Puede haber sido eliminado o el vínculo es inválido."
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, documento = documento) }
            }
        }
    }

    /** RF-14: el acceso (descarga/visualizacion) queda registrado (simulado; bitacora real en P08). */
    fun registrarAccesoDescarga() {
        _uiState.update { it.copy(accesoRegistrado = true) }
    }

    /** Factory para crear el ViewModel con el argumento de navegacion (sin Hilt aun). */
    companion object {
        fun factory(idDocumento: Int, repo: DocumentRepository = DocumentRepository()): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DetalleViewModel(repo, idDocumento) as T
            }
    }
}
