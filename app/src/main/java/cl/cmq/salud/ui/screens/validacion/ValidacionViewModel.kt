// ui/screens/validacion/ValidacionViewModel.kt
package cl.cmq.salud.ui.screens.validacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.permissions.PermisosPorPerfil
import cl.cmq.salud.domain.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * P06 Validacion (RF-15 a RF-17): bandeja de documentos pendientes y
 * registro de la decision de jefatura. MVVM: la View solo dibuja y emite
 * eventos; las reglas (comentario obligatorio en rechazo/correccion) viven aqui.
 *
 * Separacion de perfiles: solo los perfiles con facultad de validar (hoy ADM,
 * RF-07) pueden listar la bandeja y registrar decisiones. El grafo ya reserva
 * la ruta; aqui se vuelve a exigir el permiso antes de cada accion sensible
 * (defensa en profundidad: no basta con ocultar el destino).
 */
class ValidacionViewModel(
    private val repo: DocumentRepository = AppContainer.documentRepository,
    private val sesion: SesionActual? = AppContainer.sesionActual.value
) : ViewModel() {

    companion object {
        private const val LARGO_MIN_COMENTARIO = 5
    }

    private val _uiState = MutableStateFlow(ValidacionUiState())
    val uiState: StateFlow<ValidacionUiState> = _uiState.asStateFlow()

    /** ¿El perfil activo puede validar? (RF-15/RF-07) */
    private val puedeValidar: Boolean =
        sesion?.let { PermisosPorPerfil.puedeValidar(it.perfil) } ?: false

    init { cargarPendientes() }

    fun cargarPendientes() {
        // Defensa en profundidad: sin permiso no se cargan datos de la bandeja.
        if (!puedeValidar) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    pendientes = emptyList(),
                    errorPermiso = "Tu perfil no tiene permiso para revisar documentos (RF-15)."
                )
            }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorPermiso = null) }
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
        // Defensa en profundidad (RF-07/RF-15): una decision solo se registra
        // si el perfil activo tiene facultad de validar.
        if (!puedeValidar) {
            _uiState.update {
                it.copy(
                    comentarioError = "Tu perfil no tiene permiso para registrar decisiones de revisión."
                )
            }
            return
        }
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
