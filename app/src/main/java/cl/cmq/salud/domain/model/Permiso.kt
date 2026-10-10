// domain/model/Permiso.kt
package cl.cmq.salud.domain.model

/**
 * E5 Permiso del modelo de datos: solicitud de permiso/autorizacion del
 * funcionario con documento de respaldo y validacion de jefatura
 * (RF-15 a RF-17).
 */
data class Permiso(
    val idPermiso: Int,
    val idFuncionario: Int,           // FK -> Funcionario
    val idDocumentoRespaldo: Int? = null, // FK -> Documento (relacion 1:1)
    val tipoPermiso: String,          // administrativo, medico, maternidad, etc.
    val fechaInicio: String,          // DATE
    val fechaFin: String,             // DATE
    val estado: EstadoPermiso = EstadoPermiso.PENDIENTE,
    val idUsuarioValida: Int? = null, // FK -> Usuario (jefatura/RRHH que valida)
    val fechaValidacion: String? = null
)

enum class EstadoPermiso { PENDIENTE, APROBADO, RECHAZADO }
