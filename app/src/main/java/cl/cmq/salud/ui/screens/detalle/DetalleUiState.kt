// ui/screens/detalle/DetalleUiState.kt
package cl.cmq.salud.ui.screens.detalle

import cl.cmq.salud.domain.model.Documento

data class DetalleUiState(
    val isLoading: Boolean = true,
    val documento: Documento? = null,
    val error: String? = null,
    // RF-14 / RN-08: registro de acceso al documento (simulado hasta P08)
    val accesoRegistrado: Boolean = false,
    // Separacion de perfiles: permisos efectivos del perfil sobre ESTE
    // documento (RF-07 / minimo privilegio). La View los usa para mostrar u
    // ocultar los botones, y el ViewModel los vuelve a exigir antes de
    // ejecutar la accion (defensa en profundidad).
    val puedeEditar: Boolean = false,
    val puedeEliminar: Boolean = false,
    // Semana 10: eliminación con confirmación. showConfirmarEliminar abre el
    // diálogo; documentoEliminado == true indica que el borrado ya se aplicó
    // y la pantalla debe volver al expediente.
    val showConfirmarEliminar: Boolean = false,
    val documentoEliminado: Boolean = false,
    val errorEliminar: String? = null
)
