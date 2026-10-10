// domain/model/Funcionario.kt
package cl.cmq.salud.domain.model

/**
 * Funcionario del Area Salud (entidad E2 del modelo de datos, subconjunto
 * usado por la consulta de expediente). Datos ficticios (RN-03).
 */
data class Funcionario(
    val idFuncionario: Int,
    val rut: String,
    val nombreCompleto: String,
    val cargo: String,
    val centroSalud: String
)
