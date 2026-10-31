package cl.cmq.salud.data.local

import cl.cmq.salud.data.room.database.CmqSaludDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.cmq.salud.data.room.entities.DocumentoEntity
import cl.cmq.salud.data.room.entities.FuncionarioEntity
import java.time.Instant

/**
 * Semilla de datos de demostración (ficticia/sintética, RNF-09).
 *
 * Se carga en [CmqSaludDatabase] cuando se crea por primera vez.
 * Los datos son ficticios para el caso de estudio de Quilpué — Área Salud
 * y no representan antecedentes reales de funcionarios.
 */
object SeedData {

    private val funcionarios = listOf(
        FuncionarioEntity(
            rut = "18.765.432-7",
            nombreCompleto = "Juan Pérez",
            cargo = "Enfermero",
            profesion = "Enfermería",
            centroSalud = "CESFAM Quilpué",
            tipoContrato = "Planta",
            fechaIngreso = Instant.parse("2026-02-01T00:00:00Z").epochSecond,
            correo = "juan.perez@cmq.cl",
            perfilRol = "FUN"
        )
    )

    private val documentos = listOf(
        DocumentoEntity(
            idDocumento = 1,
            rutFuncionario = "18.765.432-7",
            rutCargador = "18.765.432-7",
            tipoDocumento = "PDF",
            estado = "PENDIENTE",
            fechaCarga = Instant.parse("2026-09-20T00:00:00Z").epochSecond,
            fechaModificacion = Instant.parse("2026-09-20T00:00:00Z").epochSecond,
            nombreArchivo = "Certificado_Capacitacion.pdf",
            uriLocal = "content://media/external/images/media/1",
            hash = "a1b2c3d4e5f67890",
            descripcion = "Certificado de capacitación en salud"
        ),
        DocumentoEntity(
            idDocumento = 2,
            rutFuncionario = "18.765.432-7",
            rutCargador = "18.765.432-7",
            tipoDocumento = "PDF",
            estado = "VIGENTE",
            fechaCarga = Instant.parse("2026-03-12T00:00:00Z").epochSecond,
            fechaModificacion = Instant.parse("2026-03-12T00:00:00Z").epochSecond,
            nombreArchivo = "Contrato_Trabajo.pdf",
            uriLocal = "content://media/external/images/media/2",
            hash = "b2c3d4e5f6789012",
            descripcion = "Contrato de trabajo anual"
        ),
        DocumentoEntity(
            idDocumento = 3,
            rutFuncionario = "18.765.432-7",
            rutCargador = "18.765.432-7",
            tipoDocumento = "PDF",
            estado = "PENDIENTE_CORRECCION",
            fechaCarga = Instant.parse("2026-09-18T00:00:00Z").epochSecond,
            fechaModificacion = Instant.parse("2026-09-18T00:00:00Z").epochSecond,
            nombreArchivo = "Permiso_Medico.pdf",
            uriLocal = "content://media/external/images/media/3",
            hash = "c3d4e5f678901234",
            descripcion = "Permiso médico 28-09-2026"
        ),
        DocumentoEntity(
            idDocumento = 4,
            rutFuncionario = "18.765.432-7",
            rutCargador = "18.765.432-7",
            tipoDocumento = "JPG",
            estado = "RECHAZADO",
            fechaCarga = Instant.parse("2026-09-05T00:00:00Z").epochSecond,
            fechaModificacion = Instant.parse("2026-09-05T00:00:00Z").epochSecond,
            nombreArchivo = "Anexo_Contrato.jpg",
            uriLocal = "content://media/external/images/media/4",
            hash = "d4e5f67890123456",
            descripcion = "Anexo del contrato"
        )
    )

    fun load(db: SupportSQLiteDatabase) {
        funcionarios.forEach { funcionario ->
            db.execSQL(
                """INSERT OR REPLACE INTO funcionario
                    (rut, nombreCompleto, cargo, profesion, centroSalud, tipoContrato, fechaIngreso, correo, perfilRol)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent(),
                arrayOf<Any?>(
                    funcionario.rut,
                    funcionario.nombreCompleto,
                    funcionario.cargo,
                    funcionario.profesion,
                    funcionario.centroSalud,
                    funcionario.tipoContrato,
                    funcionario.fechaIngreso,
                    funcionario.correo,
                    funcionario.perfilRol
                )
            )
        }
        documentos.forEach { documento ->
            db.execSQL(
                """INSERT OR REPLACE INTO documento
                    (idDocumento, rutFuncionario, rutCargador, tipoDocumento, estado, fechaCarga,
                     fechaModificacion, nombreArchivo, uriLocal, hash, descripcion)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent(),
                arrayOf<Any?>(
                    documento.idDocumento,
                    documento.rutFuncionario,
                    documento.rutCargador,
                    documento.tipoDocumento,
                    documento.estado,
                    documento.fechaCarga,
                    documento.fechaModificacion,
                    documento.nombreArchivo,
                    documento.uriLocal,
                    documento.hash,
                    documento.descripcion
                )
            )
        }
    }
}
