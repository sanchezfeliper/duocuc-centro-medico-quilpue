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
    object Expediente : AppDestinos("expediente")
    object Carga : AppDestinos("carga")
    object Revision : AppDestinos("revision")
    object Detalle : AppDestinos("detalle/{idDocumento}") {
        /** Construye la ruta concreta con el documento seleccionado. */
        fun crearRuta(idDocumento: Int): String = "detalle/$idDocumento"
        const val ARG_ID = "idDocumento"
    }

    /**
     * Semana 10: edición de un documento existente. Reutiliza el formulario de
     * carga en modo edición, recibiendo el ID del registro por la ruta.
     */
    object EditarCarga : AppDestinos("carga/editar/{idDocumento}") {
        fun crearRuta(idDocumento: Int): String = "carga/editar/$idDocumento"
        const val ARG_ID = "idDocumento"
    }
}
