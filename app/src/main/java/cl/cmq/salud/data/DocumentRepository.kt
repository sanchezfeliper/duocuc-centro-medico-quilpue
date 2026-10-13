// data/DocumentRepository.kt
package cl.cmq.salud.data

import cl.cmq.salud.data.local.DocumentoLocalDataSource
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Funcionario

/**
 * Repositorio de documentos y funcionarios.
 *
 * Desde la Semana 9 delega el almacenamiento en la fuente de datos local
 * (SQLite en el dispositivo), de modo que la información persiste entre
 * ejecuciones de la app. La API pública se mantiene igual, por lo que los
 * ViewModels no cambian y los tests pueden inyectar un doble de prueba.
 */
class DocumentRepository(
    private val local: DocumentoLocalDataSource
) {

    /** Busca el funcionario por RUT (E2). */
    suspend fun buscarFuncionario(rut: String): Funcionario? = local.buscarFuncionario(rut.trim())

    /** Expediente del funcionario: solo SUS documentos (RF-12). */
    suspend fun buscarPorRut(rut: String): List<Documento> =
        local.buscarDocumentosPorRut(rut.trim())

    /** Consulta un documento por su ID (Meta 6 - destino Detalle P04). */
    suspend fun buscarPorId(idDocumento: Int): Documento? = local.buscarDocumentoPorId(idDocumento)

    /**
     * Guarda un nuevo documento cargado (Meta 2).
     * El RUT ingresado en el formulario determina el funcionario asociado:
     * si es la primera carga para ese RUT, se registra el funcionario (RF-05
     * pendiente) con datos por completar.
     */
    suspend fun guardarDocumento(documento: Documento): Documento {
        val rut = documento.rutFuncionario.trim()
        val funcionario = local.buscarFuncionario(rut)
            ?: local.insertarFuncionario(
                Funcionario(
                    idFuncionario = 0,
                    rut = rut,
                    nombreCompleto = "Funcionario $rut",
                    cargo = "Por registrar",
                    centroSalud = "Por registrar"
                )
            )
        return local.insertarDocumento(documento.copy(idFuncionario = funcionario.idFuncionario))
    }

    /** Bandeja de jefatura: documentos en espera de decisión (RF-15). */
    suspend fun listarPendientes(): List<Documento> = local.listarPendientes()

    /**
     * Total de documentos que hoy viven en el almacenamiento local del
     * dispositivo (Semana 9.2). Lo usan las pantallas para mostrar el origen
     * de los datos que el usuario está viendo.
     */
    suspend fun contarDocumentos(): Int = local.contarDocumentos()

    /**
     * Registra la decisión de validación (RF-16 aprobar/rechazar, RF-17
     * solicitar corrección) y deja el comentario del revisor en el expediente.
     * Devuelve el documento actualizado, o null si el ID no existe.
     */
    suspend fun registrarDecision(
        idDocumento: Int,
        nuevoEstado: EstadoDocumento,
        comentario: String
    ): Documento? {
        val actual = local.buscarDocumentoPorId(idDocumento) ?: return null
        return local.actualizarDocumento(
            actual.copy(
                estado = nuevoEstado,
                comentarioValidacion = comentario.ifBlank { null }
            )
        )
    }
}
