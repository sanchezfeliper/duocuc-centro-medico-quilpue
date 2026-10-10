// domain/model/Capacitacion.kt
package cl.cmq.salud.domain.model

/**
 * E6 Capacitacion del modelo de datos: curso o perfeccionamiento del
 * funcionario con certificado asociado (RF-10, RF-15).
 */
data class Capacitacion(
    val idCapacitacion: Int,
    val idFuncionario: Int,               // FK -> Funcionario
    val idDocumentoCertificado: Int? = null, // FK -> Documento (relacion 1:1)
    val nombreCurso: String,
    val institucion: String? = null,
    val horas: Int = 0,
    val fechaRealizacion: String? = null, // DATE
    val estado: EstadoCapacitacion = EstadoCapacitacion.PENDIENTE
)

enum class EstadoCapacitacion { PENDIENTE, VALIDADO, RECHAZADO }
