// data/room/entities/FuncionarioEntity.kt
package cl.cmq.salud.data.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room de E2 Funcionario (tabla `funcionario`).
 *
 * La clave primaria es `rut` (forma 12.345.678-9): el documento de requisitos
 * lo define como identificador único del expediente. Exponer el RUT como PK
 * (y no un `id_funcionario` autonumérico) hace que el expediente de un
 * funcionario exista aunque todavía no tenga documentos asociados.
 *
 * Las columnas opcionales del dominio (nacimiento, situación contractual,
 * correo, perfil) se declaran nullable para no perder información al
 * reconstruir el funcionario desde la base.
 */
@Entity(tableName = "funcionario")
data class FuncionarioEntity(
    @PrimaryKey
    val rut: String,

    val nombreCompleto: String,

    val cargo: String,

    val centroSalud: String,

    val profesion: String?,

    val tipoContrato: String?,

    val fechaIngreso: String?,

    val fechaNacimiento: String?,

    val situacionContractual: String?,

    val correo: String?,

    val perfilRol: String?
)
