// di/AppContainer.kt
package cl.cmq.salud.di

import android.content.Context
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.AlmacenFotosLocales
import cl.cmq.salud.data.local.CmqSqliteHelper
import cl.cmq.salud.data.local.SqliteDocumentoLocalDataSource

/**
 * Contenedor de dependencias (Meta Semana 9).
 *
 * Inyecta manualmente la instancia unica del repositorio respaldado por SQLite.
 * Es la antesala de Hilt: los ViewModels piden la dependencia a este contenedor
 * en lugar de construirla, y en los tests se sigue inyectando un doble de
 * prueba por constructor.
 */
object AppContainer {

    private var repositorio: DocumentRepository? = null
    private var almacenFotosInterno: AlmacenFotosLocales? = null

    val documentRepository: DocumentRepository
        get() = repositorio
            ?: error("AppContainer no inicializado: llame a inicializar(context) en Application.onCreate")

    /** Almacen de las fotografias capturadas con la camara (Semana 9.3, RF-09). */
    val almacenFotos: AlmacenFotosLocales
        get() = almacenFotosInterno
            ?: error("AppContainer no inicializado: llame a inicializar(context) en Application.onCreate")

    /** Idempotente: se llama una vez desde CmqSaludApp. */
    fun inicializar(context: Context) {
        val app = context.applicationContext
        if (repositorio == null) {
            val helper = CmqSqliteHelper(app)
            repositorio = DocumentRepository(SqliteDocumentoLocalDataSource(helper))
        }
        if (almacenFotosInterno == null) {
            almacenFotosInterno = AlmacenFotosLocales(app)
        }
    }
}
