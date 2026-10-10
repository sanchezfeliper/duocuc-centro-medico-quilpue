// domain/model/Documento.kt
package cl.cmq.salud.domain.model

/**
 * E4 Documento del modelo de datos preliminar (Meta 7: la entidad disenada
 * se refleja en Kotlin; sin persistencia Room por ahora).
 *
 * rutFuncionario viaja desde el formulario (Meta 6): el RUT determina a que
 * expediente pertenece el documento.
 */
data class Documento(
    val idDocumento: Int,
    val idFuncionario: Int,             // FK -> Funcionario (E2)
    val rutFuncionario: String,         // RUT del funcionario asociado (dato del formulario, Meta 6)
    val tipo: String,                   // PDF, JPG, PNG
    val nombreArchivo: String,
    val estado: EstadoDocumento,
    val fechaCarga: String,             // ISO yyyy-MM-dd
    val usuarioCarga: String,           // responsable de la carga (RN-08)
    val descripcion: String? = null,
    // Atributos E4 incorporados en Meta 7 (coherencia con el modelo disenado)
    val fechaVencimiento: String? = null,      // DATE nullable; politica pendiente (duda D-08)
    val comentarioValidacion: String? = null,  // comentario del revisor (RF-16/RF-17)
    val hashArchivo: String? = null,           // SHA-256 para detectar duplicados (RF-11)
    val urlSharePoint: String? = null          // referencia unica sin copias (RN-05, simulada)
)

/**
 * Estados del ciclo de vida del documento (E4).
 * PENDIENTE_CORRECCION corresponde a RF-17 (solicitar correccion) y entra al
 * modelo como extension documentada del estado definido en el MER.
 */
enum class EstadoDocumento {
    VIGENTE, PENDIENTE, APROBADO, RECHAZADO, PENDIENTE_CORRECCION, VENCIDO;

    /** Etiqueta legible para badges de UI. */
    val etiqueta: String
        get() = when (this) {
            PENDIENTE_CORRECCION -> "Pendiente corrección"
            else -> name.lowercase().replaceFirstChar { it.uppercase() }
        }
}
