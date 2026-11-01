// ui/screens/perfil/PerfilUiState.kt
package cl.cmq.salud.ui.screens.perfil

import cl.cmq.salud.domain.model.Perfil

/**
 * Estado de la pantalla Perfil (P-personal, punto 6 del plan).
 *
 * Muestra los datos de la PERSONA logueada (nombre, RUT, cargo, centro,
 * perfil, correo), distintos del historial de documentos, que vive en la
 * pantalla Expediente. Los datos laborales se leen de la base por RUT
 * (E2 Funcionario); si el funcionario aún no está registrado, quedan vacíos.
 */
data class PerfilUiState(
    val rut: String = "",
    val nombreCompleto: String = "",
    val correoInstitucional: String = "",
    val perfil: Perfil = Perfil.FUN,
    val cargo: String = "",
    val centroSalud: String = "",
    val cargando: Boolean = false
) {
    val perfilNombre: String get() = perfil.nombre

    /** ¿Hay datos laborales del funcionario en la base? Si no, se avisa. */
    val tieneDatosLaborales: Boolean get() = cargo.isNotBlank() || centroSalud.isNotBlank()
}
