// data/local/SemillaDocumentos.kt
package cl.cmq.salud.data.local

import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Funcionario

/**
 * Datos de demostración que se cargan la primera vez que se abre la base
 * local (RN-03: datos ficticios o sintéticos, nunca reales).
 *
 * Se mantiene en un único lugar para que la base SQLite y el doble de
 * prueba de los tests partan del mismo expediente.
 */
object SemillaDocumentos {

    fun funcionarios(): List<Funcionario> = listOf(
        Funcionario(
            idFuncionario = 101,
            rut = "18.765.432-7",
            nombreCompleto = "Juan Pérez",
            cargo = "Enfermero",
            centroSalud = "CESFAM Quilpué"
        )
    )

    fun documentos(): List<Documento> = listOf(
        Documento(1, 101, "18.765.432-7", "PDF", "Contrato de trabajo",
            EstadoDocumento.VIGENTE, "2026-03-12", "RRHH"),
        Documento(2, 101, "18.765.432-7", "PDF", "Certificado capacitacion",
            EstadoDocumento.PENDIENTE, "2026-09-20", "J. Perez",
            fechaVencimiento = "2026-12-20"),
        Documento(3, 101, "18.765.432-7", "PDF", "Permiso medico",
            EstadoDocumento.APROBADO, "2026-09-18", "J. Perez"),
        Documento(4, 101, "18.765.432-7", "JPG", "Anexo contrato",
            EstadoDocumento.RECHAZADO, "2026-09-05", "M. Gonzalez")
    )
}
