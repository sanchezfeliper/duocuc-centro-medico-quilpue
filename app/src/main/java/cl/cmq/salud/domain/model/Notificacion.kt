// domain/model/Notificacion.kt
package cl.cmq.salud.domain.model

/**
 * E8 Notificacion del modelo de datos: alerta enviada a un usuario sobre un
 * evento del flujo documental (RF-21 a RF-23).
 */
data class Notificacion(
    val idNotificacion: Int,
    val idUsuarioDestino: Int,   // FK -> Usuario
    val idDocumento: Int? = null, // FK -> Documento (nullable)
    val tipo: TipoNotificacion,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean = false,
    val fechaEnvio: String       // DATETIME
)

enum class TipoNotificacion { PENDIENTE, APROBACION, RECHAZO, VENCIMIENTO }
