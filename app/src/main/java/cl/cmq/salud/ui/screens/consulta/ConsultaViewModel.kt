// ui/screens/consulta/ConsultaViewModel.kt
package cl.cmq.salud.ui.screens.consulta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.validation.ValidadorRut
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConsultaViewModel(
    private val repo: DocumentRepository = DocumentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultaUiState())
    val uiState: StateFlow<ConsultaUiState> = _uiState.asStateFlow()

    fun onRutChange(value: String) {
        _uiState.update { it.copy(rut = value, errorMessage = null) }
    }

    fun onFiltroChange(f: FiltroDocumento) {
        _uiState.update { it.copy(filtroActivo = f) }
        aplicarFiltro()
    }

    fun onBuscar() {
        val state = _uiState.value
        if (state.rut.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese RUT o nombre") }
            return
        }
        // Meta 5: la validacion de RUT (formato + digito verificador) vive en el
        // dominio compartido; aqui solo se consume y se traduce a estado de UI.
        val errorRut = ValidadorRut.validar(state.rut)
        if (errorRut != null) {
            _uiState.update { it.copy(errorMessage = errorRut) }
            return
        }
        _uiState.update { it.copy(isBuscando = true, errorMessage = null) }
        viewModelScope.launch {
            val docs = repo.buscarPorRut(state.rut)
            val funcionario = repo.buscarFuncionario(state.rut)

            if (docs.isEmpty() && funcionario == null) {
                // Meta 6: estado vacio real (CA-06 de la Meta 1, antes pendiente)
                _uiState.update {
                    it.copy(
                        isBuscando = false,
                        funcionario = null,
                        documentos = emptyList(),
                        documentosFiltrados = emptyList(),
                        errorMessage = "No se encontró expediente para el RUT ${state.rut.trim()}. " +
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
                        documentos = docs
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
                FiltroDocumento.PENDIENTES -> state.documentos.filter { it.estado == EstadoDocumento.PENDIENTE }
                FiltroDocumento.VENCIDOS -> state.documentos.filter { it.estado == EstadoDocumento.VENCIDO }
            }
            state.copy(documentosFiltrados = filtrada)
        }
    }
}
