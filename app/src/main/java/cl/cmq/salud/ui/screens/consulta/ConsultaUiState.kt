// ui/screens/consulta/ConsultaUiState.kt
package cl.cmq.salud.ui.screens.consulta

import cl.cmq.salud.domain.model.Documento

data class ConsultaUiState(
    val rut: String = "",
// ui/screens/consulta/ConsultaUiState.kt
    package cl.cmq.salud.ui.screens.consulta

import cl.cmq.salud.domain.model.Documento

data class ConsultaUiState(
    val rut: String = "",
    val filtroActivo: FiltroDocumento = FiltroDocumento.TODOS,
    val funcionario: FuncionarioResumen? = null,
    val documentos: List<Documento> = emptyList(),
    val isBuscando: Boolean = false,
    val errorMessage: String? = null
)

data class FuncionarioResumen(
    val rut: String,
    val nombreCompleto: String,
    val cargo: String,
    val centroSalud: String
)

enum class FiltroDocumento(val label: String) {
    TODOS("Todos"), VIGENTES("Vigentes"),
    PENDIENTES("Pendientes"), VENCIDOS("Vencidos")
}
