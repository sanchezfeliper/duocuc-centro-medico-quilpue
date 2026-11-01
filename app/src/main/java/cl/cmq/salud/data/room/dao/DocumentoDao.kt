// data/room/dao/DocumentoDao.kt
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
 * DAO Room de E4 Documento y E2 Funcionario.
 *
 * Expone las consultas que exige el documento de requisitos y las que ya
 * consumía el repositorio a través de [cl.cmq.salud.data.local.DocumentoLocalDataSource]:
 *  - getDocumentosPorRut(rut)         -> expediente del funcionario (RF-12)
 *  - getDocumentosPorCargador(rut)    -> documentos subidos por el usuario (RF-31)
 *  - getDocumentosPorEstado(estado)   -> bandeja de jefatura (RF-15)
 *  - contarDocumentos()               -> total en el dispositivo (Semana 9.2)
 *
 * Todas las operaciones son `suspend`: Room las ejecuta fuera del hilo
 * principal, por lo que el data source no necesita abrir su propio
 * Dispatchers.IO.
 */
@Dao
interface DocumentoDao {

    @Query("SELECT * FROM documento WHERE rutFuncionario = :rut ORDER BY fechaCarga DESC, idDocumento DESC")
    suspend fun getDocumentosPorRut(rut: String): List<DocumentoEntity>

    @Query("SELECT * FROM documento WHERE rutCargador = :rut ORDER BY fechaCarga DESC, idDocumento DESC")
    suspend fun getDocumentosPorCargador(rut: String): List<DocumentoEntity>

    @Query("SELECT * FROM documento WHERE estado = :estado ORDER BY fechaCarga DESC, idDocumento DESC")
    suspend fun getDocumentosPorEstado(estado: String): List<DocumentoEntity>

    @Query("SELECT * FROM documento WHERE idDocumento = :idDocumento")
    suspend fun getDocumentoById(idDocumento: Long): DocumentoEntity?

    @Query("SELECT COUNT(*) FROM documento")
    suspend fun contarDocumentos(): Int

    @Query("SELECT * FROM funcionario WHERE rut = :rut")
    suspend fun getFuncionarioByRut(rut: String): FuncionarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFuncionario(funcionario: FuncionarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(documento: DocumentoEntity): Long

    @Update
    suspend fun actualizar(documento: DocumentoEntity)

    @Delete
    suspend fun eliminar(documento: DocumentoEntity)
}
