// test/cl/cmq/salud/data/local/FakeDocumentoLocalDataSource.kt
package cl.cmq.salud.data.local

import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Funcionario

/**
 * Doble de prueba en memoria de la fuente local (Meta Semana 9).
 *
 * Permite probar el repositorio y los ViewModels en la JVM con el mismo
 * contrato que la implementación SQLite, sin Android ni base de datos real.
 * Parte de la misma semilla ficticia que la app, por lo que el expediente
 * de demostración (documento #1) existe igual.
 */
class FakeDocumentoLocalDataSource : DocumentoLocalDataSource {

    private val funcionarios = SemillaDocumentos.funcionarios().toMutableList()
    private val documentos = SemillaDocumentos.documentos().toMutableList()

    private var proximoDocumentoId = (documentos.maxOfOrNull { it.idDocumento } ?: 0) + 1
    private var proximoFuncionarioId = (funcionarios.maxOfOrNull { it.idFuncionario } ?: 0) + 1

    override suspend fun buscarFuncionario(rut: String): Funcionario? =
        funcionarios.firstOrNull { it.rut == rut.trim() }

    override suspend fun insertarFuncionario(funcionario: Funcionario): Funcionario {
        val nuevo = funcionario.copy(idFuncionario = proximoFuncionarioId++)
        funcionarios.add(nuevo)
        return nuevo
    }

    override suspend fun buscarDocumentosPorRut(rut: String): List<Documento> =
        documentos.filter { it.rutFuncionario == rut.trim() }.sortedByDescending { it.idDocumento }

    override suspend fun buscarDocumentosPorCargador(rut: String): List<Documento> =
        documentos.filter { it.usuarioCarga == rut.trim() }.sortedByDescending { it.idDocumento }

    override suspend fun buscarDocumentoPorId(idDocumento: Int): Documento? =
        documentos.firstOrNull { it.idDocumento == idDocumento }

    override suspend fun insertarDocumento(documento: Documento): Documento {
        val nuevo = documento.copy(idDocumento = proximoDocumentoId++)
        documentos.add(0, nuevo)
        return nuevo
    }

    override suspend fun actualizarDocumento(documento: Documento): Documento? {
        val indice = documentos.indexOfFirst { it.idDocumento == documento.idDocumento }
        if (indice == -1) return null
        documentos[indice] = documento
        return documento
    }

    override suspend fun eliminarDocumento(idDocumento: Int): Boolean =
        documentos.removeAll { it.idDocumento == idDocumento }

    override suspend fun contarDocumentos(): Int = documentos.size

    override suspend fun listarPendientes(): List<Documento> =
        documentos
            .filter {
                it.estado == EstadoDocumento.PENDIENTE ||
                    it.estado == EstadoDocumento.PENDIENTE_CORRECCION
            }
            .sortedByDescending { it.idDocumento }
}
