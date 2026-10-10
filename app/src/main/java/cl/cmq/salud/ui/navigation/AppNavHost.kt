// ui/navigation/AppNavHost.kt
package cl.cmq.salud.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.cmq.salud.ui.screens.carga.CargaDocumentoScreen
import cl.cmq.salud.ui.screens.consulta.ConsultaExpedienteScreen
import cl.cmq.salud.ui.screens.home.HomeScreen
import cl.cmq.salud.ui.screens.login.LoginScreen

/**
 * Grafo de navegacion de la app (Meta 5).
 *
 * Unico lugar donde se cablean pantallas y transiciones: las pantallas
 * exponen eventos (lambdas) y este grafo decide a donde navegar. Los
 * Toast son feedback temporal para destinos aun no implementados
 * (P04 Detalle, P06 Validacion, P08 Bitacora); al existir, cada uno
 * tendra su ruta propia.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = AppDestinos.Login.route
    ) {
        composable(AppDestinos.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinos.Home.route) {
                        // Login deja de estar en el back stack: Atras desde Home no vuelve al login
                        popUpTo(AppDestinos.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinos.Home.route) {
            HomeScreen(
                onNavigateConsulta = { navController.navigate(AppDestinos.Consulta.route) },
                onNavigateCarga = { navController.navigate(AppDestinos.Carga.route) },
                onNavigateValidacion = {
                    Toast.makeText(context, "Próximamente: Validaciones pendientes (P06)", Toast.LENGTH_SHORT).show()
                },
                onNavigateBitacora = {
                    Toast.makeText(context, "Próximamente: Bitácora de auditoría (P08)", Toast.LENGTH_SHORT).show()
                },
                onCerrarSesion = {
                    navController.navigate(AppDestinos.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinos.Consulta.route) {
            ConsultaExpedienteScreen(
                onVolver = { navController.popBackStack() },
                onDocumentoClick = { idDocumento ->
                    Toast.makeText(context, "Documento seleccionado: #$idDocumento (P04 próximamente)", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable(AppDestinos.Carga.route) {
            CargaDocumentoScreen(
                onVolver = { navController.popBackStack() },
                onCargaExitosa = { idDocumento ->
                    Toast.makeText(context, "Documento #$idDocumento registrado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.navigate(AppDestinos.Home.route) {
                        // Reinicia la pila en Home: la pantalla de carga sale del back stack
                        popUpTo(AppDestinos.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
