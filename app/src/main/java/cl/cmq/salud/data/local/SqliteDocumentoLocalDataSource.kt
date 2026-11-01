// data/local/SqliteDocumentoLocalDataSource.kt
package cl.cmq.salud.data.local

import cl.cmq.salud.data.local.CmqSqliteHelper.Companion.TABLA_DOCUMENTOS
import cl.cmq.salud.data.local.CmqSqliteHelper.Companion.TABLA_FUNCIONARIOS
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Funcionario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Persistencia local sobre SQLite (Meta Semana 9).
 *
 * Todo el acceso a disco ocurre en Dispatchers.IO para no bloquear la UI.
 * Los documentos y funcionarios sobreviven al cierre de la aplicación.
 */
class SqliteDocumentoLocalDataSource(
    private val helper: CmqSqliteHelper
) : DocumentoLocalDataSource {

    override suspend fun buscarFuncionario(rut: String): Funcionario? = withContext(Dispatchers.IO) {
        helper.readableDatabase.query(
            TABLA_FUNCIONARIOS, COLUMNAS_FUNCIONARIO, "rut = ?", arrayOf(rut.trim()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toFuncionario() else null
        }
    }

    override suspend fun insertarFuncionario(funcionario: Funcionario): Funcionario =
        withContext(Dispatchers.IO) {
            val id = helper.writableDatabase.insert(
                TABLA_FUNCIONARIOS, null, funcionario.toContentValues(conId = false)
            )
            funcionario.copy(idFuncionario = id.toInt())
        }

    override suspend fun buscarDocumentosPorRut(rut: String): List<Documento> =
        withContext(Dispatchers.IO) {
            helper.readableDatabase.query(
                TABLA_DOCUMENTOS, COLUMNAS_DOCUMENTO, "rut_funcionario = ?", arrayOf(rut.trim()),
                null, null, "id_documento DESC"
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add(cursor.toDocumento())
                }
            }
        }

    override suspend fun buscarDocumentoPorId(idDocumento: Int): Documento? =
        withContext(Dispatchers.IO) {
            helper.readableDatabase.query(
                TABLA_DOCUMENTOS, COLUMNAS_DOCUMENTO, "id_documento = ?", arrayOf(idDocumento.toString()),
                null, null, null
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.toDocumento() else null
            }
        }

    override suspend fun insertarDocumento(documento: Documento): Documento =
        withContext(Dispatchers.IO) {
            val id = helper.writableDatabase.insert(
                TABLA_DOCUMENTOS, null, documento.toContentValues(conId = false)
            )
            documento.copy(idDocumento = id.toInt())
        }

    override suspend fun actualizarDocumento(documento: Documento): Documento? =
        withContext(Dispatchers.IO) {
            val filas = helper.writableDatabase.update(
                TABLA_DOCUMENTOS,
                documento.toContentValues(conId = false),
                "id_documento = ?",
                arrayOf(documento.idDocumento.toString())
            )
            if (filas > 0) documento else null
        }

    override suspend fun eliminarDocumento(idDocumento: Int): Boolean =
        withContext(Dispatchers.IO) {
            val filas = helper.writableDatabase.delete(
                TABLA_DOCUMENTOS,
                "id_documento = ?",
                arrayOf(idDocumento.toString())
            )
            filas > 0
        }

    override suspend fun contarDocumentos(): Int = withContext(Dispatchers.IO) {
        helper.readableDatabase
            .rawQuery("SELECT COUNT(*) FROM $TABLA_DOCUMENTOS", null)
            .use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
    }

    override suspend fun listarPendientes(): List<Documento> = withContext(Dispatchers.IO) {
        val estados = listOf(EstadoDocumento.PENDIENTE.name, EstadoDocumento.PENDIENTE_CORRECCION.name)
        val seleccion = "estado IN (?, ?)"
        helper.readableDatabase.query(
            TABLA_DOCUMENTOS, COLUMNAS_DOCUMENTO, seleccion, estados.toTypedArray(),
            null, null, "id_documento DESC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toDocumento())
            }
        }
    }
}
