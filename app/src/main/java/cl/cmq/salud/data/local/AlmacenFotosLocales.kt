// data/local/AlmacenFotosLocales.kt
package cl.cmq.salud.data.local

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Almacenamiento de las fotografias capturadas con la camara (Semana 9.3, RF-09).
 *
 * Los archivos se guardan en el almacenamiento interno privado de la app
 * (filesDir/fotos): no requieren permisos de almacenamiento, sobreviven al
 * cierre de la aplicacion y ningun otro proceso puede leerlos. La base SQLite
 * guarda solo la ruta relativa (Documento.rutaLocal), nunca la imagen.
 *
 * Es capa de datos (recibe Context), no ViewModel: los ViewModels siguen sin
 * conocer el sistema de archivos.
 */
class AlmacenFotosLocales(private val context: Context) {

    companion object {
        const val CARPETA = "fotos"
    }

    private fun carpeta(): File = File(context.filesDir, CARPETA).apply { if (!exists()) mkdirs() }

    /** Crea un archivo nuevo con nombre unico para la proxima captura. */
    fun nuevoArchivo(): File {
        val marca = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(carpeta(), "captura_$marca.jpg")
    }

    /** Ruta relativa (la que se persiste en la base) de un archivo del almacen. */
    fun rutaRelativa(archivo: File): String = "$CARPETA/${archivo.name}"

    /** Resuelve una ruta relativa guardada a un archivo del dispositivo. */
    fun archivoDe(rutaRelativa: String): File = File(context.filesDir, rutaRelativa)

    /** True si la ruta relativa apunta a un archivo existente y no vacio. */
    fun existe(rutaRelativa: String?): Boolean {
        if (rutaRelativa.isNullOrBlank()) return false
        val archivo = archivoDe(rutaRelativa)
        return archivo.exists() && archivo.length() > 0
    }

    /** Huella SHA-256 del archivo (RF-11). Cadena vacia si no se puede leer. */
    fun huella(archivo: File): String =
        runCatching { HuellaArchivo.sha256Hex(archivo.readBytes()) }.getOrDefault("")
}
