// domain/model/Bitacora.kt
package cl.cmq.salud.domain.model

/**
 * E7 Bitacora del modelo de datos: registro de auditoria de accesos y
 * modificaciones (RF-18 a RF-20). Solo insercion: ningun registro puede
 * modificarse ni eliminarse (RNF-03).
 */
data class Bitacora(
    val idBitacora: Int,
    val idUsuario: Int,            // FK -> Usuario (quien realizo la accion)
    val idDocumento: Int? = null,  // FK -> Documento (nullable)
    val accion: AccionBitacora,
    val recurso: String? = null,
    val ipOrigen: String? = null,
    val dispositivo: String? = null,
    val fechaHora: String          // DATETIME, no modificable (RNF-03)
)

enum class AccionBitacora { LOGIN, CARGA, CONSULTA, MODIFICACION, DESCARGA, RECHAZO }
