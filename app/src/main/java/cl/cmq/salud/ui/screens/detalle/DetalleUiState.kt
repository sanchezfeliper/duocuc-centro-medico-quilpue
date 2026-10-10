// ui/screens/detalle/DetalleUiState.kt
package cl.cmq.salud.ui.screens.detalle

import cl.cmq.salud.domain.model.Documento

data class DetalleUiState(
    val isLoading: Boolean = true,
    val documento: Documento? = null,
    val error: String? = null,
    // RF-14 / RN-08: registro de acceso al documento (simulado hasta P08)
    val accesoRegistrado: Boolean = false
)
