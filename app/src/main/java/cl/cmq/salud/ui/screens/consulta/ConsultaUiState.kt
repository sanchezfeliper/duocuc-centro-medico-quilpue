// ui/screens/consulta/ConsultaUiState.kt
package cl.cmq.salud.ui.screens.consulta

import cl.cmq.salud.domain.model.Documento


data class ConsultaUiState(
    val rut: String = "",
    val filtroActivo: FiltroDocumento = FiltroDocumento.TODOS,
    val funcionario: FuncionarioResumen? = null,
    val documentos: List<Documento> = emptyList(),
    // Meta 5: el filtrado es logica del ViewModel; la View solo dibuja este listado
    val documentosFiltrados: List<Documento> = emptyList(),
    val isBuscando: Boolean = false,
    // Semana 9.2: total de documentos guardados en el dispositivo, para dejar
    // explícito que el expediente que se muestra viene del almacenamiento local.
    val totalEnAlmacenamiento: Int = 0,
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
