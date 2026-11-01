// di/AppContainer.kt
package cl.cmq.salud.di

import android.content.Context
import cl.cmq.salud.data.DocumentRepository
import cl.cmq.salud.data.local.AlmacenFotosLocales
import cl.cmq.salud.data.local.RoomDocumentoLocalDataSource
import cl.cmq.salud.data.room.database.CmqSaludDatabase
import cl.cmq.salud.domain.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Contenedor de dependencias (Meta Semana 9).
 *
 * Inyecta manualmente la instancia unica del repositorio respaldado por SQLite.
 * Es la antesala de Hilt: los ViewModels piden la dependencia a este contenedor
 * en lugar de construirla, y en los tests se sigue inyectando un doble de
 * prueba por constructor.
 *
 * Tambien es la fuente unica de la sesion activa (separacion de perfiles):
 * quien entra queda aqui y las pantallas leen de aqui, en vez de repetir datos
 * ficticios dispersos. Solo vive en memoria: RF-02 exige limpiarla al cerrar.
 */
object AppContainer {

    private var repositorio: DocumentRepository? = null
    private var almacenFotosInterno: AlmacenFotosLocales? = null

    private val _sesionActual = MutableStateFlow<SesionActual?>(null)

    /**
     * Sesion activa observable: null mientras nadie ha iniciado sesion.
     * Las pantallas la observan para reaccionar al ingreso y al cierre.
     */
    val sesionActual: StateFlow<SesionActual?> = _sesionActual.asStateFlow()

    val documentRepository: DocumentRepository
        get() = repositorio
            ?: error("AppContainer no inicializado: llame a inicializar(context) en Application.onCreate")

    /** Almacen de las fotografias capturadas con la camara (Semana 9.3, RF-09). */
    val almacenFotos: AlmacenFotosLocales
        get() = almacenFotosInterno
            ?: error("AppContainer no inicializado: llame a inicializar(context) en Application.onCreate")

    /** Abre sesion: Login la llama al resolver credenciales validas. */
    fun iniciarSesion(sesion: SesionActual) {
        _sesionActual.value = sesion
    }

    /** Cierra sesion y borra la identidad en memoria (RF-02). */
    fun cerrarSesion() {
        _sesionActual.value = null
    }

    /** Idempotente: se llama una vez desde CmqSaludApp. */
    fun inicializar(context: Context) {
        val app = context.applicationContext
        if (repositorio == null) {
            val database = CmqSaludDatabase.getInstance(app)
            repositorio = DocumentRepository(RoomDocumentoLocalDataSource(database.documentoDao))
        }
        if (almacenFotosInterno == null) {
            almacenFotosInterno = AlmacenFotosLocales(app)
        }
    }
}
