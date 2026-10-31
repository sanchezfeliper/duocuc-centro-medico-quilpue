package cl.cmq.salud.data.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room de [Documento] (tabla `documento`).
 *
 * Los campos coinciden con la Tabla 2 del documento de requisitos:
 * idDocumento, rutFuncionario, rutCargador, tipoDocumento, estado,
 * fechaCarga, fechaModificacion, nombreArchivo, uriLocal, hash,
 * descripcion.
 *
 * - `rutFuncionario` es clave foránea a `funcionario(rut)` con eliminación
 *   restringida (RNF-11) y está indexada.
 * - `hash` y `descripcion` son opcionales según el caso de uso.
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
    indices = [Index("rutFuncionario")]
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

    val descripcion: String?
)
