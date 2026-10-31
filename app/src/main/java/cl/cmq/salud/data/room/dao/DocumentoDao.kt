package cl.cmq.salud.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.cmq.salud.data.room.entities.DocumentoEntity
import cl.cmq.salud.data.room.entities.FuncionarioEntity

/**
 * DAO Room para la entidad [DocumentoEntity].
 *
 * Expone, como mínimo, las consultas exigidas por el documento de
 * requisitos:
 *  - getDocumentosPorRut(rut)   -> consulta por RUT del propietario
 *  - getDocumentosPorCargador(rut) -> consulta por RUT del cargador
 *  - getDocumentosPorEstado(estado) -> consulta por estado
 * Además de inserción y actualización.
 */
@Dao
interface DocumentoDao {

    @Query("SELECT * FROM documento WHERE rutFuncionario = :rut ORDER BY fechaCarga DESC")
    suspend fun getDocumentosPorRut(rut: String): List<DocumentoEntity>

    @Query("SELECT * FROM documento WHERE rutCargador = :rut ORDER BY fechaCarga DESC")
    suspend fun getDocumentosPorCargador(rut: String): List<DocumentoEntity>

    @Query("SELECT * FROM documento WHERE estado = :estado ORDER BY fechaCarga DESC")
    suspend fun getDocumentosPorEstado(estado: String): List<DocumentoEntity>

    @Query("SELECT COUNT(*) FROM documento")
    suspend fun contarDocumentos(): Int

    @Query("SELECT * FROM documento WHERE idDocumento = :idDocumento")
    suspend fun getDocumentoById(idDocumento: Long): DocumentoEntity?

    @Query("SELECT * FROM funcionario WHERE rut = :rut")
    suspend fun getFuncionarioByRut(rut: String): FuncionarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFuncionario(funcionario: FuncionarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(documento: DocumentoEntity): Long

    @Update
    suspend fun actualizar(documento: DocumentoEntity)

    @Delete
    suspend fun eliminar(documento: DocumentoEntity)
}
