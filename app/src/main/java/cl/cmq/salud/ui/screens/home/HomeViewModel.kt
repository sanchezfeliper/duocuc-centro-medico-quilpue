package cl.cmq.salud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Home por rol (separación de perfiles).
 *
 * La identidad ya no está hardcodeada: sale de la sesión activa y los
 * contadores que se muestran dependen del perfil. Administración ve la
 * bandeja de revisión global (RF-15); el funcionario ve el estado de su
 * propio expediente y no recibe datos de terceros (mínimo privilegio).
 */
class HomeViewModel(
    private val repo: DocumentRepository = AppContainer.documentRepository,
    private val sesion: SesionActual? = AppContainer.sesionActual.value
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            rut = sesion?.rut.orEmpty(),
            nombreUsuario = sesion?.nombreCompleto.orEmpty(),
            perfil = sesion?.perfil ?: HomeUiState().perfil
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { cargarResumen() }

    /** Carga el resumen que corresponde al perfil activo, desde el repositorio. */
    private fun cargarResumen() {
        val sesionActiva = sesion ?: return
        viewModelScope.launch {
            val base = HomeUiState(
                rut = sesionActiva.rut,
                nombreUsuario = sesionActiva.nombreCompleto,
                perfil = sesionActiva.perfil,
                centroSalud = repo.buscarFuncionario(sesionActiva.rut)?.centroSalud.orEmpty(),
                documentosEnAlmacenamiento = repo.contarDocumentos()
            )
            _uiState.value = base.copy(
                validacionesPendientes = if (base.puedeRevisar) repo.listarPendientes().size else 0,
                misDocumentos = if (base.puedeRevisar) 0 else repo.buscarPorRut(sesionActiva.rut).size,
                documentosPorVencer = documentosPorVencer(sesionActiva)
            )
        }
    }

    /** Refresca los contadores al volver a Home (ej: tras cargar o validar documentos). */
    fun refrescarContador() {
        val sesionActiva = sesion ?: return
        viewModelScope.launch {
            val estado = _uiState.value
            _uiState.value = estado.copy(
                validacionesPendientes = if (estado.puedeRevisar) repo.listarPendientes().size else 0,
                documentosEnAlmacenamiento = repo.contarDocumentos(),
                misDocumentos = if (estado.puedeRevisar) 0 else repo.buscarPorRut(sesionActiva.rut).size
            )
        }
    }

    /**
     * Documentos del expediente propio que están por vencer. El enum VENCIDO
     * existe en el modelo, pero su cálculo contra la fecha de hoy es deuda
     * conocida (RF-23); aqui se cuenta lo que la jefatura marcó como pendiente
     * de decisión para que el aviso no sea un número inventado.
     */
    private suspend fun documentosPorVencer(sesionActiva: SesionActual): Int {
        val propios = repo.buscarPorRut(sesionActiva.rut)
        return propios.count {
            it.estado == EstadoDocumento.PENDIENTE ||
                it.estado == EstadoDocumento.PENDIENTE_CORRECCION
        }
    }

    /** Solicita el cierre de sesión: Home reacciona navegando al login. */
    fun cerrarSesion() {
        _uiState.value = _uiState.value.copy(isSessionActive = false)
    }
}
