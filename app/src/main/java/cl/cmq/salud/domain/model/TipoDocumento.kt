// domain/model/TipoDocumento.kt
package cl.cmq.salud.domain.model

/**
 * E3 TipoDocumento del modelo de datos: catalogo de categorias de documentos
 * (RF-10). Los diasVigencia quedan nullable mientras se resuelve la duda
 * D-08 (politica institucional de vigencias); el equipo no inventa plazos.
 */
data class TipoDocumento(
    val idTipoDocumento: Int,
    val nombre: String,               // UK: Contrato, Permiso, Capacitacion, etc.
    val descripcion: String? = null,
    val diasVigencia: Int? = null,    // dias de vigencia por defecto (nullable, D-08)
    val requiereValidacion: Boolean = false // si el tipo requiere aprobacion de jefatura
)
