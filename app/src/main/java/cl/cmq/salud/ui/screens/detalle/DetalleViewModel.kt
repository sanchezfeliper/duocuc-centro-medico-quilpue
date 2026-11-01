// ui/screens/detalle/DetalleViewModel.kt
package cl.cmq.salud.ui.screens.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.permissions.PermisosPorPerfil
import cl.cmq.salud.domain.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Meta 6: ViewModel del destino P04 Detalle. Recibe el ID del documento
 * como argumento de navegacion y resuelve el dato contra la fuente unica
 * (DocumentRepository), no via objetos transportados entre pantallas.
 *
 * Separacion de perfiles: la sesion determina si el perfil puede editar o
 * eliminar ESTE documento (RF-07 / minimo privilegio). Los permisos se
 * calculan al cargar el documento y se vuelven a exigir antes de cada accion
 * (defensa en profundidad: ocultar el boton no basta).
 */
class DetalleViewModel(
    private val repo: DocumentRepository,
    private val idDocumento: Int,
    private val sesion: SesionActual? = AppContainer.sesionActual.value
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        documento = documento,
                        puedeEditar = puedeEditar(documento),
                        puedeEliminar = puedeEliminar(documento)
                    )
                }
            }
        }
    }

    /** ¿El perfil activo puede modificar este documento, segun su estado y dueno? (RF-07) */
    private fun puedeEditar(documento: Documento): Boolean {
        val s = sesion ?: return false
        return PermisosPorPerfil.puedeEditarDocumento(
            perfil = s.perfil,
            estado = documento.estado,
            rutDocumento = documento.rutFuncionario,
            rutSesion = s.rut
        )
    }

    private fun puedeEliminar(documento: Documento): Boolean {
        val s = sesion ?: return false
        return PermisosPorPerfil.puedeEliminarDocumento(
            perfil = s.perfil,
            estado = documento.estado,
            rutDocumento = documento.rutFuncionario,
            rutSesion = s.rut
        )
    }

    /** RF-14: el acceso (descarga/visualizacion) queda registrado (simulado; bitacora real en P08). */
    fun registrarAccesoDescarga() {
        _uiState.update { it.copy(accesoRegistrado = true) }
    }

    // ------------------------------------------------------------------
    // Semana 10: eliminar el documento (operación D del CRUD).
    // Flujo de dos pasos: abrir el diálogo de confirmación y, solo si el usuario
    // confirma, borrar en el almacenamiento local y marcar el estado de éxito.
    // ------------------------------------------------------------------

    fun abrirConfirmacionEliminar() {
        // Defensa en profundidad (RF-07): aunque la View oculte el boton, el
        // ViewModel revalida el permiso sobre el documento cargado antes de
        // habilitar el dialogo de borrado.
        val documento = _uiState.value.documento
        if (documento == null || !puedeEliminar(documento)) {
            _uiState.update {
                it.copy(
                    errorEliminar = "Tu perfil no tiene permiso para eliminar este documento."
                )
            }
            return
        }
        _uiState.update { it.copy(showConfirmarEliminar = true, errorEliminar = null) }
    }

    fun cancelarEliminar() {
        _uiState.update { it.copy(showConfirmarEliminar = false, errorEliminar = null) }
    }

    fun eliminarDocumento() {
        _uiState.update { it.copy(showConfirmarEliminar = false) }
        viewModelScope.launch {
            // Revalida el permiso contra el estado vigente en el repositorio:
            // entre abrir el dialogo y confirmar, otro perfil pudo cambiar el
            // estado del documento (p. ej. aprobarlo) y retirar el permiso.
            val actual = repo.buscarPorId(idDocumento)
            if (actual == null) {
                _uiState.update {
                    it.copy(
                        errorEliminar = "No se pudo eliminar el documento #$idDocumento: ya no existe en el almacenamiento local."
                    )
                }
                return@launch
            }
            if (!puedeEliminar(actual)) {
                _uiState.update {
                    it.copy(
                        errorEliminar = "Tu perfil no tiene permiso para eliminar este documento: " +
                            "solo se pueden retirar los propios y mientras sigan pendientes."
                    )
                }
                return@launch
            }
            val borrado = repo.eliminarDocumento(idDocumento)
            if (borrado) {
                _uiState.update { it.copy(documentoEliminado = true) }
            } else {
                _uiState.update {
                    it.copy(
                        errorEliminar = "No se pudo eliminar el documento #$idDocumento: ya no existe en el almacenamiento local."
                    )
                }
            }
        }
    }

    /** Factory para crear el ViewModel con el argumento de navegacion (sin Hilt aun). */
    companion object {
        fun factory(
            idDocumento: Int,
            repo: DocumentRepository = AppContainer.documentRepository,
            sesion: SesionActual? = AppContainer.sesionActual.value
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DetalleViewModel(repo, idDocumento, sesion) as T
            }
    }
}
