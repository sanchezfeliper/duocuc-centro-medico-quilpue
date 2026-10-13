package cl.cmq.salud

import android.app.Application
import cl.cmq.salud.di.AppContainer

/**
 * Application del proyecto: prepara el almacenamiento local (SQLite) antes
 * de que cualquier pantalla lo use (Meta Semana 9 - Persistencia local).
 */
class CmqSaludApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.inicializar(this)
    }
}
