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
            centroSalud = "CESFAM Quilpué",
            profesion = "Enfermería",
            tipoContrato = "Planta",
            fechaIngreso = "2026-02-01",
            correo = "juan.perez@cmq.cl",
            perfilRol = "FUN"
        ),
        // Cuenta de administración de demostración (CatalogoCuentasDemo.ADMIN).
        // Se registra sin documentos: así el expediente existe aunque todavía
        // no tenga archivos y Perfil puede mostrar sus datos laborales.
        Funcionario(
            idFuncionario = 102,
            rut = "12.345.678-9",
            nombreCompleto = "Maria Gonzalez",
            cargo = "Administrativo",
            centroSalud = "CESFAM Quilpué",
            profesion = "Administración",
            tipoContrato = "Contrata",
            fechaIngreso = "2025-11-15",
            correo = "maria.gonzalez@cmq.cl",
            perfilRol = "ADM"
        )
    )

    fun documentos(): List<Documento> = listOf(
        Documento(1, 101, "18.765.432-7", "PDF", "contrato_trabajo_juan_perez.pdf",
            EstadoDocumento.VIGENTE, "2026-03-12", "RRHH",
            descripcion = "Contrato de trabajo vigente del funcionario.",
            rutaLocal = "content://cmq.seed/expediente/contrato_trabajo_juan_perez.pdf"),
        Documento(2, 101, "18.765.432-7", "PDF", "certificado_capacitacion_primeros_auxilios.pdf",
            EstadoDocumento.PENDIENTE, "2026-09-20", "J. Perez",
            descripcion = "Certificado de capacitación en primeros auxilios.",
            fechaVencimiento = "2026-12-20",
            rutaLocal = "content://cmq.seed/expediente/certificado_capacitacion_primeros_auxilios.pdf"),
        Documento(3, 101, "18.765.432-7", "PDF", "permiso_medico_reposo.pdf",
            EstadoDocumento.APROBADO, "2026-09-18", "J. Perez",
            descripcion = "Permiso médico por reposo de tres días.",
            rutaLocal = "content://cmq.seed/expediente/permiso_medico_reposo.pdf"),
        Documento(4, 101, "18.765.432-7", "JPG", "anexo_contrato_horas_extra.jpg",
            EstadoDocumento.RECHAZADO, "2026-09-05", "M. Gonzalez",
            descripcion = "Anexo de contrato por horas extraordinarias.",
            rutaLocal = "content://cmq.seed/expediente/anexo_contrato_horas_extra.jpg")
    )
}
