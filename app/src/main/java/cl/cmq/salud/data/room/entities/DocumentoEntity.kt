// data/room/entities/DocumentoEntity.kt
package cl.cmq.salud.data.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room de E4 Documento (tabla `documento`).
 *
 * Las columnas coinciden con la Tabla 2 del documento de requisitos:
 * idDocumento, rutFuncionario, rutCargador, tipoDocumento, estado,
 * fechaCarga, fechaModificacion, nombreArchivo, uriLocal, hash, descripcion.
 *
 * - `rutFuncionario` es clave foránea a `funcionario(rut)` con eliminación
 *   restringida (RNF-11) y está indexada por ser la consulta más frecuente.
 * - Las columnas opcionales (fecha de vencimiento, comentario de validación,
 *   referencia SharePoint) se conservan para no perder los atributos que el
 *   dominio ya usaba en el esquema SQLite anterior.
 * - Las fechas se guardan como epoch (Long) porque es el formato nativo de
 *   Room/SQLite para ordenar y comparar sin parsear texto; el mapeo a ISO
 *   ocurre en el data source, manteniendo el dominio en String.
 */
@Entity(
    tableName = "documento",
    foreignKeys = [
        ForeignKey(
            entity = FuncionarioEntity::class,
            parentColumns = ["rut"],
            childColumns = ["rutFuncionario"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("rutFuncionario"), Index("rutCargador")]
)
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true)
    val idDocumento: Long,

    val rutFuncionario: String,

    val rutCargador: String,

    val tipoDocumento: String,

    val estado: String,

    val fechaCarga: Long,

    val fechaModificacion: Long,

    val nombreArchivo: String,

    val uriLocal: String,

    val hash: String?,

    val descripcion: String?,

    val fechaVencimiento: Long?,

    val comentarioValidacion: String?,

    val urlSharePoint: String?
)
