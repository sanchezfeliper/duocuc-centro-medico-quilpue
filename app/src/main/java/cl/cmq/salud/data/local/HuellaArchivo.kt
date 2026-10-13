// data/local/HuellaArchivo.kt
package cl.cmq.salud.data.local

import java.security.MessageDigest

/**
 * Huella SHA-256 de un archivo (Semana 9.3, RF-11).
 *
 * El modelo de datos E4 define Documento.hashArchivo "para detectar
 * duplicados". Aqui se calcula la huella real del contenido capturado con la
 * camara, de modo que el campo deja de ser decorativo. Se mantiene como
 * funcion pura (bytes -> texto) para poder probarla sin Android.
 */
object HuellaArchivo {

    /** Devuelve el SHA-256 del contenido en hexadecimal minuscula (64 caracteres). */
    fun sha256Hex(contenido: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(contenido)
            .joinToString("") { "%02x".format(it.toInt() and 0xFF) }
}
