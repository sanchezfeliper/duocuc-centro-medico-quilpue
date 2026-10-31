package cl.cmq.salud.data.local

import cl.cmq.salud.data.room.dao.DocumentoDao
import cl.cmq.salud.data.room.entities.DocumentoEntity
import cl.cmq.salud.data.room.entities.FuncionarioEntity
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Funcionario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Implementación de [DocumentoLocalDataSource] sobre Room.
 *
 * Convierte entities Room -> dominio (y viceversa) en cada operación,
 * manteniendo la abstracción de la capa de datos. El dominio conserva
 * `idDocumento: Int` y `fechaCarga: String` según el código actual;
 * la capa de datos realiza la conversión a `Long` para Room.
 */
class RoomDocumentoLocalDataSource(
    private val dao: DocumentoDao
) : DocumentoLocalDataSource {

    override suspend fun buscarFuncionario(rut: String): Funcionario? = withContext(Dispatchers.IO) {
        dao.getFuncionarioByRut(rut.trim())?.toDomain()
    }

    override suspend fun insertarFuncionario(funcionario: Funcionario): Funcionario = withContext(Dispatchers.IO) {
        val entity = funcionario.toEntity()
        val id = dao.insertarFuncionario(entity)
        entity.toDomain(id.toInt())
    }

    override suspend fun buscarDocumentosPorRut(rut: String): List<Documento> = withContext(Dispatchers.IO) {
        dao.getDocumentosPorRut(rut.trim()).map { it.toDomain() }
    }

    override suspend fun buscarDocumentosPorCargador(rut: String): List<Documento> = withContext(Dispatchers.IO) {
        dao.getDocumentosPorCargador(rut.trim()).map { it.toDomain() }
    }

    override suspend fun buscarDocumentoPorId(idDocumento: Int): Documento? = withContext(Dispatchers.IO) {
        dao.getDocumentoById(idDocumento.toLong())?.toDomain()
    }

    override suspend fun insertarDocumento(documento: Documento): Documento = withContext(Dispatchers.IO) {
        val entity = documento.toEntity()
        val id = dao.insertar(entity)
        entity.toDomain(id.toInt())
    }

    override suspend fun actualizarDocumento(documento: Documento): Documento? = withContext(Dispatchers.IO) {
        val entity = documento.toEntity()
        dao.actualizar(entity)
        documento
    }

    override suspend fun listarPendientes(): List<Documento> = withContext(Dispatchers.IO) {
        val pendientes = dao.getDocumentosPorEstado("PENDIENTE") + dao.getDocumentosPorEstado("PENDIENTE_CORRECCION")
        pendientes.map { it.toDomain() }
    }

    override suspend fun contarDocumentos(): Int = withContext(Dispatchers.IO) {
        dao.contarDocumentos()
    }
}

/* ------------------------------------------------------------------ */
/* Mapeo entity -> dominio (extension functions)                      */
/* ------------------------------------------------------------------ */

private fun FuncionarioEntity.toDomain(idFuncionario: Int = rut.hashCode()): Funcionario = Funcionario(
    idFuncionario = idFuncionario,
    rut = rut,
    nombreCompleto = nombreCompleto,
    cargo = cargo,
    centroSalud = centroSalud,
    profesion = profesion,
    tipoContrato = tipoContrato,
    fechaIngreso = Instant.ofEpochSecond(fechaIngreso).atZone(ZoneId.systemDefault()).toLocalDate().toString(),
    fechaNacimiento = null,
    situacionContractual = null,
    correo = correo,
    perfilRol = perfilRol
)

private fun DocumentoEntity.toDomain(idDocumento: Int = this.idDocumento.toInt()): Documento = Documento(
    idDocumento = idDocumento,
    idFuncionario = rutFuncionario.hashCode(),
    rutFuncionario = rutFuncionario,
    tipo = tipoDocumento,
    nombreArchivo = nombreArchivo,
    estado = EstadoDocumento.valueOf(estado),
    fechaCarga = Instant.ofEpochSecond(fechaCarga).atZone(ZoneId.systemDefault()).toLocalDate().toString(),
    usuarioCarga = rutCargador,
    descripcion = descripcion
)

/* ------------------------------------------------------------------ */
/* Mapeo dominio -> entity (extension functions)                      */
/* ------------------------------------------------------------------ */

private fun Funcionario.toEntity(): FuncionarioEntity = FuncionarioEntity(
    rut = rut,
    nombreCompleto = nombreCompleto,
    cargo = cargo,
    profesion = profesion ?: "",
    centroSalud = centroSalud,
    tipoContrato = tipoContrato ?: "",
    fechaIngreso = java.time.Instant.parse(fechaIngreso ?: "1970-01-01T00:00:00Z").epochSecond,
    correo = correo.orEmpty(),
    perfilRol = perfilRol.orEmpty()
)

private fun Documento.toEntity(): DocumentoEntity = DocumentoEntity(
    idDocumento = idDocumento.toLong(),
    rutFuncionario = rutFuncionario,
    rutCargador = usuarioCarga,
    tipoDocumento = tipo,
    estado = estado.name,
    fechaCarga = LocalDate.parse(fechaCarga)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond,
    fechaModificacion = LocalDate.parse(fechaCarga)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond,
    nombreArchivo = nombreArchivo,
    uriLocal = rutaLocal ?: "",
    hash = hashArchivo,
    descripcion = descripcion
)
