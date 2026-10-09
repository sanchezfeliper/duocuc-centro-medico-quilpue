// data/DocumentRepository.kt
package cl.cmq.salud.data

import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.delay

class DocumentRepository {

    companion object {
        private val documentosMemoria = mutableListOf(
            Documento(1, 101, "PDF", "Contrato de trabajo",
                EstadoDocumento.VIGENTE, "2026-03-12", "RRHH"),
            Documento(2, 101, "PDF", "Certificado capacitacion",
                EstadoDocumento.PENDIENTE, "2026-09-20", "J. Perez"),
            Documento(3, 101, "PDF", "Permiso medico",
                EstadoDocumento.APROBADO, "2026-09-18", "J. Perez"),
            Documento(4, 101, "JPG", "Anexo contrato",
                EstadoDocumento.RECHAZADO, "2026-09-05", "M. Gonzalez")
        )
        private var nextId = 5
    }

    /** Datos ficticios (RN-03). En la siguiente iteracion: SharePoint + Room. */
    suspend fun buscarPorRut(rut: String): List<Documento> {
        delay(600) // simula latencia de red
        return documentosMemoria.toList()
    }

    /** Guarda un nuevo documento cargado (Meta 2 - Formulario funcional). */
    suspend fun guardarDocumento(documento: Documento): Documento {
        delay(800) // simula subida y registro en SharePoint / API
        val nuevoDoc = documento.copy(idDocumento = nextId++)
        documentosMemoria.add(0, nuevoDoc)
        return nuevoDoc
    }
}


