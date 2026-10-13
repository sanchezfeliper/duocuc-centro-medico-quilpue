// data/local/DocumentoLocalDataSource.kt
package cl.cmq.salud.data.local

import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.Funcionario

/**
 * Fuente de datos local (persistencia en el dispositivo).
 *
 * Abstrae el motor de almacenamiento del resto de la app: el repositorio
 * depende de esta interfaz, no de SQLite. Permite además sustituirla por un
 * doble de prueba en los tests unitarios (sin Android).
 */
interface DocumentoLocalDataSource {

    suspend fun buscarFuncionario(rut: String): Funcionario?

    /** Inserta el funcionario y devuelve la entidad con el ID asignado. */
    suspend fun insertarFuncionario(funcionario: Funcionario): Funcionario

    suspend fun buscarDocumentosPorRut(rut: String): List<Documento>

    suspend fun buscarDocumentoPorId(idDocumento: Int): Documento?

    /** Inserta el documento y devuelve la entidad con el ID (folio) asignado. */
    suspend fun insertarDocumento(documento: Documento): Documento

    /** Actualiza un documento existente; null si el ID no existe. */
    suspend fun actualizarDocumento(documento: Documento): Documento?

    /** Documentos en espera de decisión de jefatura (RF-15). */
    suspend fun listarPendientes(): List<Documento>

    /**
     * Total de documentos guardados en el dispositivo (Semana 9.2).
     * Es el dato que la app muestra para hacer visible que la información
     * vive realmente en el almacenamiento local y no solo en memoria.
     */
    suspend fun contarDocumentos(): Int
}
