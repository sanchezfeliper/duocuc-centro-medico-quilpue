// ui/navigation/AppDestinos.kt
package cl.cmq.salud.ui.navigation

/**
 * Rutas de navegacion de la app (Meta 5).
 * Reemplaza al enum AppPantalla: la navegacion vive en su propio
 * paquete y MainActivity deja de conocer pantallas concretas.
 */
sealed class AppDestinos(val route: String) {
    object Login : AppDestinos("login")
    object Home : AppDestinos("home")
    object Consulta : AppDestinos("consulta")
    object Carga : AppDestinos("carga")
}
