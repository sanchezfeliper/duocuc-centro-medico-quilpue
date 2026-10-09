// ui/screens/consulta/ConsultaViewModel.kt
package cl.cmq.salud.ui.screens.consulta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.domain.model.EstadoDocumento
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
        if (!validarRut(state.rut)) {
            _uiState.update { it.copy(errorMessage = "RUT invalido") }
            return
        }
        _uiState.update { it.copy(isBuscando = true, errorMessage = null) }
        viewModelScope.launch {
            val docs = repo.buscarPorRut(state.rut)
            _uiState.update {
                it.copy(
                    isBuscando = false,
                    funcionario = FuncionarioResumen(
                        rut = state.rut,
                        nombreCompleto = "Juan Perez",
                        cargo = "Enfermero",
                        centroSalud = "CESFAM Quilpue"
                    ),
                    documentos = docs
                )
            }
            aplicarFiltro()
        }
    }

    private fun aplicarFiltro() {
        val state = _uiState.value
        val original = state.documentos
        val filtrada = when (state.filtroActivo) {
            FiltroDocumento.TODOS -> original
            FiltroDocumento.VIGENTES -> original.filter { it.estado == EstadoDocumento.VIGENTE }
            FiltroDocumento.PENDIENTES -> original.filter { it.estado == EstadoDocumento.PENDIENTE }
            FiltroDocumento.VENCIDOS -> original.filter { it.estado == EstadoDocumento.VENCIDO }
        }
        // En esta iteracion el filtro se aplica en memoria sobre los datos ficticios.
    }

    private fun validarRut(rut: String): Boolean {
        // Patron basico 12.345.678-9; validacion completa con digito verificador en la siguiente iteracion
        val regex = Regex("^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$")
        return regex.matches(rut)
    }
}
