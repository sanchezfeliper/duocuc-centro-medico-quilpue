// domain/model/Sesion.kt
package cl.cmq.salud.domain.model

/**
 * E9 Sesion del modelo de datos: sesion activa del usuario con token y
 * dispositivo (RF-01, RF-03, RF-04). Sustento del bloqueo por inactividad
 * y del registro de intentos fallidos.
 */
data class Sesion(
    val idSesion: Int,
    val idUsuario: Int,          // FK -> Usuario
    val token: String,           // UK, token cifrado
    val dispositivo: String? = null,
    val ipInicio: String? = null,
    val fechaInicio: String,     // DATETIME
    val fechaCierre: String? = null, // nullable mientras la sesion esta activa
    val estado: EstadoSesion = EstadoSesion.ACTIVA
)

enum class EstadoSesion { ACTIVA, CERRADA, EXPIRADA }
