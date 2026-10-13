// data/local/Mapeadores.kt
package cl.cmq.salud.data.local

import android.content.ContentValues
import android.database.Cursor
import cl.cmq.salud.domain.model.Documento
import cl.cmq.salud.domain.model.EstadoDocumento
import cl.cmq.salud.domain.model.Funcionario

/**
 * Conversión entre las entidades de dominio y las tablas SQLite.
 * Mantiene el modelo de dominio libre de detalles de persistencia.
 */

// ---------------------------------------------------------------------------
// Documento (E4)
// ---------------------------------------------------------------------------

fun Documento.toContentValues(conId: Boolean): ContentValues = ContentValues().apply {
    if (conId) put("id_documento", idDocumento)
    put("id_funcionario", idFuncionario)
    put("rut_funcionario", rutFuncionario)
    put("tipo", tipo)
    put("nombre_archivo", nombreArchivo)
    put("estado", estado.name)
    put("fecha_carga", fechaCarga)
    put("usuario_carga", usuarioCarga)
    put("descripcion", descripcion)
    put("fecha_vencimiento", fechaVencimiento)
    put("comentario_validacion", comentarioValidacion)
    put("hash_archivo", hashArchivo)
    put("url_sharepoint", urlSharePoint)
}

fun Cursor.toDocumento(): Documento = Documento(
    idDocumento = getInt(getColumnIndexOrThrow("id_documento")),
    idFuncionario = getInt(getColumnIndexOrThrow("id_funcionario")),
    rutFuncionario = getString(getColumnIndexOrThrow("rut_funcionario")),
    tipo = getString(getColumnIndexOrThrow("tipo")),
    nombreArchivo = getString(getColumnIndexOrThrow("nombre_archivo")),
    estado = EstadoDocumento.valueOf(getString(getColumnIndexOrThrow("estado"))),
    fechaCarga = getString(getColumnIndexOrThrow("fecha_carga")),
    usuarioCarga = getString(getColumnIndexOrThrow("usuario_carga")),
    descripcion = getString(getColumnIndexOrThrow("descripcion")),
    fechaVencimiento = getString(getColumnIndexOrThrow("fecha_vencimiento")),
    comentarioValidacion = getString(getColumnIndexOrThrow("comentario_validacion")),
    hashArchivo = getString(getColumnIndexOrThrow("hash_archivo")),
    urlSharePoint = getString(getColumnIndexOrThrow("url_sharepoint"))
)

// ---------------------------------------------------------------------------
// Funcionario (E2)
// ---------------------------------------------------------------------------

fun Funcionario.toContentValues(conId: Boolean): ContentValues = ContentValues().apply {
    if (conId) put("id_funcionario", idFuncionario)
    put("rut", rut)
    put("nombre_completo", nombreCompleto)
    put("cargo", cargo)
    put("centro_salud", centroSalud)
    put("profesion", profesion)
    put("tipo_contrato", tipoContrato)
    put("fecha_ingreso", fechaIngreso)
    put("fecha_nacimiento", fechaNacimiento)
    put("situacion_contractual", situacionContractual)
}

fun Cursor.toFuncionario(): Funcionario = Funcionario(
    idFuncionario = getInt(getColumnIndexOrThrow("id_funcionario")),
    rut = getString(getColumnIndexOrThrow("rut")),
    nombreCompleto = getString(getColumnIndexOrThrow("nombre_completo")),
    cargo = getString(getColumnIndexOrThrow("cargo")),
    centroSalud = getString(getColumnIndexOrThrow("centro_salud")),
    profesion = getString(getColumnIndexOrThrow("profesion")),
    tipoContrato = getString(getColumnIndexOrThrow("tipo_contrato")),
    fechaIngreso = getString(getColumnIndexOrThrow("fecha_ingreso")),
    fechaNacimiento = getString(getColumnIndexOrThrow("fecha_nacimiento")),
    situacionContractual = getString(getColumnIndexOrThrow("situacion_contractual"))
)

/** Columnas usadas por las consultas de documentos (evita SELECT *). */
val COLUMNAS_DOCUMENTO = arrayOf(
    "id_documento", "id_funcionario", "rut_funcionario", "tipo", "nombre_archivo",
    "estado", "fecha_carga", "usuario_carga", "descripcion", "fecha_vencimiento",
    "comentario_validacion", "hash_archivo", "url_sharepoint"
)

val COLUMNAS_FUNCIONARIO = arrayOf(
    "id_funcionario", "rut", "nombre_completo", "cargo", "centro_salud",
    "profesion", "tipo_contrato", "fecha_ingreso", "fecha_nacimiento",
    "situacion_contractual"
)
