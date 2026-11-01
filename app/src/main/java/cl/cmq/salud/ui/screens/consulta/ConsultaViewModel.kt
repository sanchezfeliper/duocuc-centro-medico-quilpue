// ui/screens/consulta/ConsultaViewModel.kt
package cl.cmq.salud.ui.screens.consulta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.di.AppContainer
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.permissions.PermisosPorPerfil
import cl.cmq.salud.domain.session.SesionActual
import cl.cmq.salud.domain.validation.ValidadorRut
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConsultaViewModel(
    private val repo: DocumentRepository = AppContainer.documentRepository,
    // Separacion de perfiles: el funcionario solo consulta su propio
    // expediente (RF-12), asi que su RUT se fija desde la sesion y el campo
    // deja de ser editable. Administracion puede buscar cualquier RUT (RF-13).
    private val sesion: SesionActual? = AppContainer.sesionActual.value
) : ViewModel() {

    private val rutFijo: Boolean = sesion
        ?.let { !PermisosPorPerfil.puedeBuscarOtrosRuts(it.perfil) && it.rut.isNotBlank() }
        ?: false

    private val _uiState = MutableStateFlow(
        ConsultaUiState(
            rutFijo = rutFijo,
            // Con RUT fijo el expediente propio ya viene escrito; ademas se
            // busca de inmediato para que el funcionario vea sus documentos
            // sin tener que pulsar Buscar.
            rut = if (rutFijo) sesion?.rut.orEmpty() else ""
        )
    )
    val uiState: StateFlow<ConsultaUiState> = _uiState.asStateFlow()

    init {
        // El funcionario entra directo a su expediente (RF-12: consulta propia).
        if (rutFijo) onBuscar()
    }

    fun onRutChange(value: String) {
        // Defensa en profundidad: aunque la UI marque el campo como de solo
        // lectura, el ViewModel ignora cualquier intento de cambiarlo.
        if (rutFijo) return
        _uiState.update { it.copy(rut = value, errorMessage = null) }
    }

    fun onFiltroChange(f: FiltroDocumento) {
        _uiState.update { it.copy(filtroActivo = f) }
        aplicarFiltro()
    }

    fun onBuscar() {
        val state = _uiState.value
        // Defensa en profundidad (RF-12): con RUT fijo, buscar solo puede
        // recaer sobre el propio expediente, nunca sobre el de un tercero.
        val rutBuscado = if (rutFijo) sesion?.rut.orEmpty() else state.rut
        if (rutBuscado.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese RUT o nombre") }
            return
        }
        // Meta 5: la validacion de RUT (formato + digito verificador) vive en el
        // dominio compartido; aqui solo se consume y se traduce a estado de UI.
        val errorRut = ValidadorRut.validar(rutBuscado)
        if (errorRut != null) {
            _uiState.update { it.copy(errorMessage = errorRut) }
            return
        }
        _uiState.update { it.copy(isBuscando = true, errorMessage = null) }
        viewModelScope.launch {
            val docs = repo.buscarPorRut(rutBuscado)
            val funcionario = repo.buscarFuncionario(rutBuscado)
            // Semana 9.2: cuántos documentos hay hoy en el dispositivo (SQLite)
            val totalLocal = repo.contarDocumentos()

            if (docs.isEmpty() && funcionario == null) {
                // Meta 6: estado vacio real (CA-06 de la Meta 1, antes pendiente)
                _uiState.update {
                    it.copy(
                        isBuscando = false,
                        funcionario = null,
                        documentos = emptyList(),
                        documentosFiltrados = emptyList(),
                        totalEnAlmacenamiento = totalLocal,
                        errorMessage = "No se encontró expediente para el RUT ${rutBuscado.trim()}. " +
                            "Verifique el RUT o cargue un documento para este funcionario."
                    )
                }
            } else {
                // Meta 6: el resumen sale del repositorio (E2), no de datos hardcodeados
                _uiState.update {
                    it.copy(
                        isBuscando = false,
                        funcionario = funcionario?.let { f ->
                            FuncionarioResumen(
                                rut = f.rut,
                                nombreCompleto = f.nombreCompleto,
                                cargo = f.cargo,
                                centroSalud = f.centroSalud
                            )
                        },
                        documentos = docs,
                        totalEnAlmacenamiento = totalLocal
                    )
                }
                aplicarFiltro()
            }
        }
    }

    /** Logica de filtrado del expediente: antes vivia en la View (Meta 5). */
    private fun aplicarFiltro() {
        _uiState.update { state ->
            val filtrada = when (state.filtroActivo) {
                FiltroDocumento.TODOS -> state.documentos
                FiltroDocumento.VIGENTES -> state.documentos.filter { it.estado == EstadoDocumento.VIGENTE }
                FiltroDocumento.PENDIENTES -> state.documentos.filter {
                    it.estado == EstadoDocumento.PENDIENTE || it.estado == EstadoDocumento.PENDIENTE_CORRECCION
                }
                FiltroDocumento.VENCIDOS -> state.documentos.filter { it.estado == EstadoDocumento.VENCIDO }
            }
            state.copy(documentosFiltrados = filtrada)
        }
    }
}
