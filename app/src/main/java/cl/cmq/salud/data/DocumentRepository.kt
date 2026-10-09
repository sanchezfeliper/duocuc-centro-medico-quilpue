// data/DocumentRepository.kt
package cl.cmq.salud.data

import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import kotlinx.coroutines.delay

class DocumentRepository {

    /** Datos ficticios (RN-03). En la siguiente iteracion: SharePoint + Room. */
    suspend fun buscarPorRut(rut: String): List<Documento> {
        delay(600) // simula latencia de red
        return listOf(
            Documento(1, 101, "PDF", "Contrato de trabajo",
                EstadoDocumento.VIGENTE, "2026-03-12", "RRHH"),
            Documento(2, 101, "PDF", "Certificado capacitacion",
                EstadoDocumento.PENDIENTE, "2026-09-20", "J. Perez"),
            Documento(3, 101, "PDF", "Permiso medico",
                EstadoDocumento.APROBADO, "2026-09-18", "J. Perez"),
            Documento(4, 101, "JPG", "Anexo contrato",
                EstadoDocumento.RECHAZADO, "2026-09-05", "M. Gonzalez")
        )
    }
}


