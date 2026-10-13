// di/AppContainer.kt
package cl.cmq.salud.di

import android.content.Context
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.CmqSqliteHelper
import cl.cmq.salud.data.local.SqliteDocumentoLocalDataSource

/**
 * Contenedor de dependencias (Meta Semana 9).
 *
 * Inyecta manualmente la instancia única del repositorio respaldado por SQLite.
 * Es la antesala de Hilt: los ViewModels piden la dependencia a este contenedor
 * en lugar de construirla, y en los tests se sigue inyectando un doble de
 * prueba por constructor.
 */
object AppContainer {

    private var repositorio: DocumentRepository? = null

    val documentRepository: DocumentRepository
        get() = repositorio
            ?: error("AppContainer no inicializado: llame a inicializar(context) en Application.onCreate")

    /** Idempotente: se llama una vez desde CmqSaludApp. */
    fun inicializar(context: Context) {
        if (repositorio == null) {
            val helper = CmqSqliteHelper(context.applicationContext)
            repositorio = DocumentRepository(SqliteDocumentoLocalDataSource(helper))
        }
    }
}
