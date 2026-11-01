// ui/screens/validacion/ValidacionUiState.kt
package cl.cmq.salud.ui.screens.validacion

import cl.cmq.salud.domain.model.Documento

data class ValidacionUiState(
    val pendientes: List<Documento> = emptyList(),
    val isLoading: Boolean = false,
    val seleccionado: Documento? = null,
    val comentario: String = "",
    val comentarioError: String? = null,
    val isProcesando: Boolean = false,
    // Meta 4: eventos unicos para Snackbar (el mensaje se repite sin problema)
    val successEventId: Long = 0,
    val ultimoMensajeExito: String? = null,
    // Separacion de perfiles: aviso cuando el perfil activo no tiene facultad
    // de validar (RF-07/RF-15). En ese caso la bandeja no se carga.
    val errorPermiso: String? = null
)
