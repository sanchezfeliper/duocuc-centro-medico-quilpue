// data/local/RoomDocumentoLocalDataSource.kt
package cl.cmq.salud.data.local

import cl.cmq.salud.data.room.dao.DocumentoDao
import cl.cmq.salud.data.room.entities.DocumentoEntity
import cl.cmq.salud.data.room.entities.FuncionarioEntity
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Funcionario

/**
 * Implementación de [DocumentoLocalDataSource] sobre Room (RNF-14).
 *
 * Sustituye a [SqliteDocumentoLocalDataSource] sin cambiar el contrato que
 * consume el repositorio. El dominio mantiene sus tipos originales
 * (`fechaCarga: String` ISO, `idDocumento: Int`); la conversión a los tipos
 * de Room (epoch `Long`, fila `Long`) ocurre aquí, en la frontera de datos.
 *
 * No hace falta envolver en `Dispatchers.IO`: las funciones del DAO son
 * `suspend` y Room las ejecuta en su propio executor.
 */
class RoomDocumentoLocalDataSource(
    private val dao: DocumentoDao
) : DocumentoLocalDataSource {

    override suspend fun buscarFuncionario(rut: String): Funcionario? =
        dao.getFuncionarioByRut(rut.trim())?.toDomain()

    override suspend fun insertarFuncionario(funcionario: Funcionario): Funcionario {
        dao.insertarFuncionario(funcionario.toEntity())
        return funcionario
    }

    override suspend fun buscarDocumentosPorRut(rut: String): List<Documento> =
        dao.getDocumentosPorRut(rut.trim()).map { it.toDomain() }

    override suspend fun buscarDocumentosPorCargador(rut: String): List<Documento> =
        dao.getDocumentosPorCargador(rut.trim()).map { it.toDomain() }

    override suspend fun buscarDocumentoPorId(idDocumento: Int): Documento? =
        dao.getDocumentoById(idDocumento.toLong())?.toDomain()

    override suspend fun insertarDocumento(documento: Documento): Documento {
        val id = dao.insertar(documento.toEntity())
        return documento.copy(idDocumento = id.toInt())
    }

    override suspend fun actualizarDocumento(documento: Documento): Documento? {
        val existe = dao.getDocumentoById(documento.idDocumento.toLong()) ?: return null
        dao.actualizar(documento.toEntity(fechaModificacion = existe.fechaModificacion))
        return documento
    }

    override suspend fun eliminarDocumento(idDocumento: Int): Boolean {
        val existente = dao.getDocumentoById(idDocumento.toLong()) ?: return false
        dao.eliminar(existente)
        return true
    }

    override suspend fun listarPendientes(): List<Documento> {
        val pendientes =
            dao.getDocumentosPorEstado(EstadoDocumento.PENDIENTE.name) +
                dao.getDocumentosPorEstado(EstadoDocumento.PENDIENTE_CORRECCION.name)
        return pendientes
            .sortedWith(compareByDescending<DocumentoEntity> { it.fechaCarga }.thenByDescending { it.idDocumento })
            .map { it.toDomain() }
    }

    override suspend fun contarDocumentos(): Int = dao.contarDocumentos()
}

/* ------------------------------------------------------------------ */
/* Mapeo entity -> dominio                                             */
/* ------------------------------------------------------------------ */

private fun FuncionarioEntity.toDomain(): Funcionario = Funcionario(
    // El RUT es la clave del expediente; el dominio conserva un id estable
    // derivado de él, ya que Room ya no usa un autonumérico id_funcionario.
    idFuncionario = rut.hashCode(),
    rut = rut,
    nombreCompleto = nombreCompleto,
    cargo = cargo,
    centroSalud = centroSalud,
    profesion = profesion,
    tipoContrato = tipoContrato,
    fechaIngreso = fechaIngreso,
    fechaNacimiento = fechaNacimiento,
    situacionContractual = situacionContractual,
    correo = correo,
    perfilRol = perfilRol
)

private fun DocumentoEntity.toDomain(): Documento = Documento(
    idDocumento = idDocumento.toInt(),
    idFuncionario = rutFuncionario.hashCode(),
    rutFuncionario = rutFuncionario,
    tipo = tipoDocumento,
    nombreArchivo = nombreArchivo,
    estado = EstadoDocumento.valueOf(estado),
    fechaCarga = epochAIso(fechaCarga),
    usuarioCarga = rutCargador,
    descripcion = descripcion,
    fechaVencimiento = fechaVencimiento?.let { epochAIso(it) },
    comentarioValidacion = comentarioValidacion,
    hashArchivo = hash,
    urlSharePoint = urlSharePoint,
    rutaLocal = uriLocal.ifBlank { null }
)

/* ------------------------------------------------------------------ */
/* Mapeo dominio -> entity                                             */
/* ------------------------------------------------------------------ */

private fun Funcionario.toEntity(): FuncionarioEntity = FuncionarioEntity(
    rut = rut,
    nombreCompleto = nombreCompleto,
    cargo = cargo,
    centroSalud = centroSalud,
    profesion = profesion,
    tipoContrato = tipoContrato,
    fechaIngreso = fechaIngreso,
    fechaNacimiento = fechaNacimiento,
    situacionContractual = situacionContractual,
    correo = correo,
    perfilRol = perfilRol
)

/**
 * Convierte el documento de dominio al entity de Room.
 *
 * [fechaModificacion] permite preservar la marca original en una edición: al
 * actualizar no debe moverse mientras que la fecha de carga tampoco cambia.
 */
private fun Documento.toEntity(fechaModificacion: Long = fechaAIso(fechaCarga)): DocumentoEntity =
    DocumentoEntity(
        idDocumento = idDocumento.toLong(),
        rutFuncionario = rutFuncionario,
        rutCargador = usuarioCarga,
        tipoDocumento = tipo,
        estado = estado.name,
        fechaCarga = fechaAIso(fechaCarga),
        fechaModificacion = fechaModificacion,
        nombreArchivo = nombreArchivo,
        uriLocal = rutaLocal.orEmpty(),
        hash = hashArchivo,
        descripcion = descripcion,
        fechaVencimiento = fechaVencimiento?.let { fechaAIso(it) },
        comentarioValidacion = comentarioValidacion,
        urlSharePoint = urlSharePoint
    )

/** Fecha ISO (`yyyy-MM-dd`) -> epoch (segundos). 0 si el texto no es una fecha. */
private fun fechaAIso(fecha: String): Long =
    runCatching {
        java.time.LocalDate.parse(fecha)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toEpochSecond()
    }.getOrDefault(0L)

/** Epoch (segundos) -> fecha ISO (`yyyy-MM-dd`). */
private fun epochAIso(epoch: Long): String =
    java.time.Instant.ofEpochSecond(epoch)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .toString()
