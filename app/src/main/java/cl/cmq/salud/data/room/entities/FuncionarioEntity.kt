package cl.cmq.salud.data.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room de [Funcionario] (tabla `funcionario`).
 *
 * La clave primaria es `rut` (forma 12.345.678-9), tal como lo especifica
 * el documento de requisitos: "RUT del funcionario, identificador único
 * del expediente". No se usa `id_funcionario` (clave fija del esquema SQLite
 * anterior a Room).
 */
@Entity(tableName = "funcionario")
data class FuncionarioEntity(
    @PrimaryKey
    val rut: String,

    val nombreCompleto: String,

    val cargo: String,

    val profesion: String,

    val centroSalud: String,

    val tipoContrato: String,

    val fechaIngreso: Long,

    val correo: String,

    val perfilRol: String
)
