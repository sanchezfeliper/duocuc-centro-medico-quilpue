package cl.cmq.salud.ui.screens.perfil

import cl.cmq.salud.domain.model.Documento

/**
 * Estado de la pantalla Perfil.
 *
 * RF-30: ficha del usuario autenticado (RUT, nombre, cargo, centro, correo, rol).
 * RF-31: lista de documentos subidos por el propio usuario, ordenados
 *        por fecha de carga descendente con tipo, estado y fecha.
 */
data class PerfilUiState(
    val rut: String = "",
    val nombreCompleto: String = "",
    val cargo: String = "",
    val centroSalud: String = "",
    val correo: String = "",
    val perfilRol: String = "",
    val documentos: List<Documento> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
