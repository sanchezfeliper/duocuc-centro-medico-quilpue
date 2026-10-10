// ui/navigation/AppDestinos.kt
package cl.cmq.salud.ui.navigation

/**
 * Rutas de navegacion de la app (Meta 5 / Meta 6).
 * La ruta de Detalle declara el argumento {idDocumento}: es el paso de
 * informacion entre pantallas exigido por la Meta 6 (Listado -> Detalle).
 */
sealed class AppDestinos(val route: String) {
    object Login : AppDestinos("login")
    object Home : AppDestinos("home")
    object Consulta : AppDestinos("consulta")
    object Carga : AppDestinos("carga")
    object Detalle : AppDestinos("detalle/{idDocumento}") {
        /** Construye la ruta concreta con el documento seleccionado. */
        fun crearRuta(idDocumento: Int): String = "detalle/$idDocumento"
        const val ARG_ID = "idDocumento"
    }
}
