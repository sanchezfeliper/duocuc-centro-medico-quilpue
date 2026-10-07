// domain/model/Documento.kt
package cl.cmq.salud.domain.model

data class Documento(
    val idDocumento: Int,
    val idFuncionario: Int,
    val tipo: String,           // PDF, JPG, PNG
    val nombreArchivo: String,
    val estado: EstadoDocumento,
    val fechaCarga: String,     // ISO yyyy-MM-dd
    val usuarioCarga: String,
    val descripcion: String? = null
)

enum class EstadoDocumento {
    VIGENTE, PENDIENTE, APROBADO, RECHAZADO, VENCIDO
}
