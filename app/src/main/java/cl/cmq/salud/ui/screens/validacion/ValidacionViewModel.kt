// ui/screens/validacion/ValidacionViewModel.kt
package cl.cmq.salud.ui.screens.validacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * P06 Validacion (RF-15 a RF-17): bandeja de documentos pendientes y
 * registro de la decision de jefatura. MVVM: la View solo dibuja y emite
 * eventos; las reglas (comentario obligatorio en rechazo/correccion) viven aqui.
 */
class ValidacionViewModel(
    private val repo: DocumentRepository = DocumentRepository()
) : ViewModel() {

    companion object {
        private const val LARGO_MIN_COMENTARIO = 5
    }

    private val _uiState = MutableStateFlow(ValidacionUiState())
    val uiState: StateFlow<ValidacionUiState> = _uiState.asStateFlow()

    init { cargarPendientes() }

    fun cargarPendientes() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val pendientes = repo.listarPendientes()
            _uiState.update { it.copy(isLoading = false, pendientes = pendientes) }
        }
    }

    fun onSeleccionar(idDocumento: Int) {
        _uiState.update { state ->
            val doc = state.pendientes.firstOrNull { it.idDocumento == idDocumento }
                ?: return@update state
            // Segundo tap sobre el mismo documento cancela la seleccion
            val yaSeleccionado = state.seleccionado?.idDocumento == idDocumento
            state.copy(
                seleccionado = if (yaSeleccionado) null else doc,
                comentario = "",
                comentarioError = null
            )
        }
    }

    fun onComentarioChange(value: String) {
        _uiState.update { it.copy(comentario = value, comentarioError = null) }
    }

    /**
     * Registra la decision (RF-16/RF-17). El comentario es obligatorio para
     * rechazar o solicitar correccion; en la aprobacion es opcional.
     */
    fun decidir(nuevoEstado: EstadoDocumento) {
        val state = _uiState.value
        val doc = state.seleccionado ?: return

        if (nuevoEstado != EstadoDocumento.APROBADO && state.comentario.trim().length < LARGO_MIN_COMENTARIO) {
            val accion = if (nuevoEstado == EstadoDocumento.RECHAZADO) "rechazar" else "solicitar corrección"
            _uiState.update {
                it.copy(
                    comentarioError = "El comentario es obligatorio para $accion: indique el motivo " +
                        "(mínimo $LARGO_MIN_COMENTARIO caracteres) para que el funcionario sepa cómo corregir."
                )
            }
            return
        }

        _uiState.update { it.copy(isProcesando = true, comentarioError = null) }
        viewModelScope.launch {
            val actualizado = repo.registrarDecision(doc.idDocumento, nuevoEstado, state.comentario.trim())
            val restantes = repo.listarPendientes()
            _uiState.update {
                it.copy(
                    isProcesando = false,
                    pendientes = restantes,
                    seleccionado = null,
                    comentario = "",
                    successEventId = it.successEventId + 1,
                    ultimoMensajeExito = if (actualizado != null) {
                        "Documento #${doc.idDocumento} ${mensaje(nuevoEstado)}"
                    } else {
                        "No se pudo registrar la decisión del documento #${doc.idDocumento}"
                    }
                )
            }
        }
    }

    private fun mensaje(estado: EstadoDocumento): String = when (estado) {
        EstadoDocumento.APROBADO -> "aprobado correctamente"
        EstadoDocumento.RECHAZADO -> "rechazado"
        EstadoDocumento.PENDIENTE_CORRECCION -> "marcado como pendiente de corrección"
        else -> "actualizado a ${estado.name}"
    }
}
